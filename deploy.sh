#!/usr/bin/env bash
#
# 后端一键发布：打包 → 上传 web.war → 远端替换并重启
#
# 用法（第一个参数是前端端口，前后端一一对应，只记前端即可）：
#   bash deploy.sh 8003              前端 dev（10.13.31.36:8003）→ 后端 /data/service/digital-zhugeng-backend-8005
#   bash deploy.sh 8003 --skip-build 跳过打包，直接发布现有 web-api/target/web.war
#   bash deploy.sh 8003 --dry-run    只做检查与计划打印，不写远端
#   bash deploy.sh 8003 --rollback   用 web.war.bak 回滚并重启
#   bash deploy.sh 8003 --dir <路径> 手动指定后端目录（未登记的环境）
#
# 说明：实际监听端口由远端 config/application.yml 决定（当前 8005，与目录名一致），
#       健康检查地址也从该配置自动推导，改端口只需改配置后执行 ./start.sh restart。
#
# 前置条件：本机已配置到目标机的免密 SSH；后端目录内有可执行的 start.sh。
# 发布期间后端服务会在 restart 时短暂中断。

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUILD_SCRIPT="${ROOT_DIR}/build.sh"
WAR_PATH="${ROOT_DIR}/web-api/target/web.war"

HOST="${DEPLOY_HOST:-root@10.13.31.235}"
FRONT_HOST="${DEPLOY_FRONT_HOST:-10.13.31.36}"
WAR_NAME="${DEPLOY_WAR_NAME:-web.war}"
HEALTH_URL="${DEPLOY_HEALTH_URL:-}"
HEALTH_TRIES="${DEPLOY_HEALTH_TRIES:-40}"
HEALTH_INTERVAL="${DEPLOY_HEALTH_INTERVAL:-3}"

PORT="${DEPLOY_PORT:-}"
DIR_OVERRIDE="${DEPLOY_DIR:-}"
SKIP_BUILD=0
DRY_RUN=0
ROLLBACK=0

usage() {
  awk 'NR > 1 && /^#/ { sub(/^# ?/, ""); print; next } NR > 1 { exit }' "${BASH_SOURCE[0]}"
}

say() { printf '\n==> %s\n' "$*"; }
die() { printf '\n[错误] %s\n' "$*" >&2; exit 1; }

# 前端端口 → 后端目录（前后端按端口一一对应）
dir_for_port() {
  case "$1" in
    8003) printf '%s' '/data/service/digital-zhugeng-backend-8005' ;;
    *)    printf '%s' '' ;;
  esac
}

while [ $# -gt 0 ]; do
  case "$1" in
    --skip-build) SKIP_BUILD=1 ;;
    --dry-run)    DRY_RUN=1 ;;
    --rollback)   ROLLBACK=1 ;;
    --host)       HOST="${2:?--host 需要一个参数}"; shift ;;
    --dir)        DIR_OVERRIDE="${2:?--dir 需要一个参数}"; shift ;;
    --health-url) HEALTH_URL="${2:?--health-url 需要一个参数}"; shift ;;
    -h|--help)    usage; exit 0 ;;
    ''|*[!0-9]*)  die "未知参数：$1（第一个参数应为前端端口：8003）" ;;
    *)            PORT="$1" ;;
  esac
  shift
done

if [ -n "$DIR_OVERRIDE" ]; then
  REMOTE_DIR="$DIR_OVERRIDE"
else
  [ -n "$PORT" ] || die "缺少前端端口，用法：bash deploy.sh 8003"
  REMOTE_DIR="$(dir_for_port "$PORT")"
  [ -n "$REMOTE_DIR" ] || die "前端端口 $PORT 没有登记后端目录，请用 --dir 指定（已登记：8003）"
fi

ssh_do() { ssh -o BatchMode=yes -o LogLevel=ERROR -o ConnectTimeout=10 "${HOST}" "$@"; }

# 从远端 config/application.yml 推出健康检查地址（端口与 context-path 随各环境配置变化）
detect_health_url() {
  ssh_do "REMOTE_DIR=$(printf '%q' "${REMOTE_DIR}") bash -s" <<'REMOTE'
set -uo pipefail
cfg="${REMOTE_DIR}/config/application.yml"
[ -f "$cfg" ] || exit 0
port=$(grep -m1 -E '^[[:space:]]+port:[[:space:]]*[0-9]+' "$cfg" | sed -E 's/.*port:[[:space:]]*([0-9]+).*/\1/')
ctx=$(grep -m1 -E 'context-path:' "$cfg" | sed -E 's/.*context-path:[[:space:]]*//' | tr -d ' \r"')
[ -n "$port" ] || exit 0
echo "http://127.0.0.1:${port}${ctx}/"
REMOTE
}

war_sha() {
  if [ -f "$1" ]; then shasum -a 256 "$1" | awk '{print $1}'; fi
}

# 远端轮询健康检查：war 启动需要时间，单次 curl 不够；/js/ 通常 302 跳登录页，2xx/3xx 都算存活
remote_wait_healthy() {
  ssh_do "HEALTH_URL=$(printf '%q' "${HEALTH_URL}") TRIES=$(printf '%q' "${HEALTH_TRIES}") INTERVAL=$(printf '%q' "${HEALTH_INTERVAL}") bash -s" <<'REMOTE'
set -uo pipefail
code=""
i=1
while [ "$i" -le "$TRIES" ]; do
  code="$(curl -s -o /dev/null -m 5 -w '%{http_code}' "$HEALTH_URL" || true)"
  case "$code" in
    2*|3*)
      echo "健康检查通过（第 ${i} 次轮询，${HEALTH_URL} 返回 ${code}）"
      exit 0 ;;
  esac
  i=$((i + 1))
  sleep "$INTERVAL"
done
echo "健康检查未通过：${HEALTH_URL} 最后返回 HTTP ${code:-无响应}（已轮询 ${TRIES} 次）" >&2
exit 1
REMOTE
}

# 前端站点可达性：只作提示，发布对象是后端，不拿它做失败判定
check_frontend() {
  local code
  code="$(curl -s -o /dev/null -m 8 -w '%{http_code}' -H "Host: ${FRONT_HOST}" "http://${FRONT_HOST}:${PORT}/" || true)"
  case "$code" in
    2*|3*) echo "前端连通：http://${FRONT_HOST}:${PORT}/ 返回 ${code}" ;;
    *) echo "[提示] 前端 http://${FRONT_HOST}:${PORT}/ 返回 ${code:-无响应}，请确认前端是否已发布" ;;
  esac
}

remote_rollback() {
  ssh_do "REMOTE_DIR=$(printf '%q' "${REMOTE_DIR}") WAR_NAME=$(printf '%q' "${WAR_NAME}") bash -s" <<'REMOTE'
set -euo pipefail
cd "$REMOTE_DIR"
[ -f "${WAR_NAME}.bak" ] || { echo "没有 ${WAR_NAME}.bak，无法回滚" >&2; exit 1; }
rm -f "${WAR_NAME}.prev"
if [ -f "$WAR_NAME" ]; then mv "$WAR_NAME" "${WAR_NAME}.prev"; fi
mv "${WAR_NAME}.bak" "$WAR_NAME"
echo "已回滚 ${REMOTE_DIR}/${WAR_NAME}（被替换的版本留在 ${WAR_NAME}.prev）"
./start.sh restart
REMOTE
}

# ---------------------------------------------------------------- 回滚
if [ "$ROLLBACK" -eq 1 ]; then
  say "回滚 ${HOST}:${REMOTE_DIR}"
  if [ "$DRY_RUN" -eq 1 ]; then
    echo "[dry-run] 将把 ${WAR_NAME}.bak 换回 ${WAR_NAME}，当前版本留作 ${WAR_NAME}.prev，然后执行 ./start.sh restart"
    exit 0
  fi
  remote_rollback
  say "健康检查"
  if ! remote_wait_healthy; then
    die "回滚后服务未通过健康检查，请登录服务器查看 ./start.sh restart 输出"
  fi
  say "回滚完成"
  exit 0
fi

# ---------------------------------------------------------------- 预检
say "预检：SSH 连通、后端目录与 start.sh（前端 ${PORT} → 后端 ${REMOTE_DIR}）"
ssh_do "REMOTE_DIR=$(printf '%q' "${REMOTE_DIR}") bash -s" <<'REMOTE' || die "远端预检失败（SSH 不通、目录不存在或缺少 start.sh）"
set -euo pipefail
[ -d "$REMOTE_DIR" ] || { echo "远端目录不存在：${REMOTE_DIR}" >&2; exit 1; }
[ -w "$REMOTE_DIR" ] || { echo "远端目录不可写：${REMOTE_DIR}" >&2; exit 1; }
[ -x "${REMOTE_DIR}/start.sh" ] || { echo "远端缺少可执行的 start.sh：${REMOTE_DIR}/start.sh" >&2; exit 1; }
avail_kb=$(df -Pk "$REMOTE_DIR" | tail -1 | awk '{print $4}')
[ "$avail_kb" -gt 1048576 ] || { echo "远端剩余空间不足 1GB（当前 $((avail_kb / 1024))MB），war 约 170MB，需容纳上传包与备份" >&2; exit 1; }
echo "远端目录可写，start.sh 可执行，剩余空间 $((avail_kb / 1024))MB"
REMOTE

if [ -z "${HEALTH_URL}" ]; then
  HEALTH_URL="$(detect_health_url || true)"
  [ -n "${HEALTH_URL}" ] || die "无法从远端 config/application.yml 推导健康检查地址，请用 --health-url 指定"
  echo "健康检查地址（由远端配置推导）：${HEALTH_URL}"
fi

# ---------------------------------------------------------------- 打包
if [ "$SKIP_BUILD" -eq 0 ]; then
  if [ "$DRY_RUN" -eq 1 ]; then
    echo "[dry-run] 将执行：bash ${BUILD_SCRIPT}"
  else
    WAR_SHA_BEFORE="$(war_sha "${WAR_PATH}" || true)"
    say "打包（bash build.sh）"
    ( cd "$ROOT_DIR" && bash ./build.sh )
    WAR_SHA_AFTER="$(war_sha "${WAR_PATH}" || true)"
    if [ -n "$WAR_SHA_BEFORE" ] && [ "$WAR_SHA_BEFORE" = "$WAR_SHA_AFTER" ]; then
      echo "提示：web.war 与打包前内容一致（源码无改动）"
    fi
  fi
fi

# 产物校验：打包失败由 mvn 退出码拦住，这里只确认 war 本身可用
if [ "$SKIP_BUILD" -eq 1 ] || [ "$DRY_RUN" -eq 0 ]; then
  [ -f "$WAR_PATH" ] || die "找不到 ${WAR_PATH}，请先打包（去掉 --skip-build）"
  WAR_BYTES="$(wc -c < "$WAR_PATH" | tr -d ' ')"
  [ "$WAR_BYTES" -gt 20971520 ] || die "web.war 只有 $((WAR_BYTES / 1024))KB，疑似打包产物异常"
  WAR_LISTING="$(unzip -l "$WAR_PATH" 2>/dev/null || true)"
  case "$WAR_LISTING" in
    *"WEB-INF/"*) echo "发布包：${WAR_PATH}（$((WAR_BYTES / 1048576))MB，已含 WEB-INF）" ;;
    *) die "web.war 内缺少 WEB-INF，中止发布" ;;
  esac
fi

if [ "$DRY_RUN" -eq 1 ]; then
  echo "[dry-run] 计划：scp web.war → ${REMOTE_DIR}/.upload-<时间戳>.war"
  echo "[dry-run] 计划：备份 ${WAR_NAME} → ${WAR_NAME}.bak，上传包挪为 ${WAR_NAME}，执行 ./start.sh restart"
  echo "[dry-run] 计划：轮询后端健康检查 ${HEALTH_URL}（最多 ${HEALTH_TRIES} 次，间隔 ${HEALTH_INTERVAL}s）"
  echo "[dry-run] 计划：提示前端 http://${FRONT_HOST}:${PORT}/ 可达性（不参与成败判定）"
  echo "[dry-run] 回滚命令：bash deploy.sh ${PORT} --rollback"
  echo
  echo "dry-run 结束，未做任何远端写入。"
  exit 0
fi

# ---------------------------------------------------------------- 上传
TS="$(date +%Y%m%d%H%M%S)"
UPLOAD_NAME=".upload-${TS}.war"

say "上传 web.war → ${HOST}:${REMOTE_DIR}/${UPLOAD_NAME}（约 $((WAR_BYTES / 1048576))MB，下方为传输进度）"
scp -o BatchMode=yes -o LogLevel=ERROR -o ConnectTimeout=10 "${WAR_PATH}" "${HOST}:${REMOTE_DIR}/${UPLOAD_NAME}"

REMOTE_BYTES="$(ssh_do "stat -c %s $(printf '%q' "${REMOTE_DIR}/${UPLOAD_NAME}") 2>/dev/null || echo 0")"
if [ "${REMOTE_BYTES}" != "${WAR_BYTES}" ]; then
  ssh_do "rm -f $(printf '%q' "${REMOTE_DIR}/${UPLOAD_NAME}")" || true
  die "上传不完整：本地 ${WAR_BYTES} 字节，远端 ${REMOTE_BYTES} 字节（已删除残包，线上 war 未动）"
fi
echo "上传完成，大小校验一致：${WAR_BYTES} 字节"

# ---------------------------------------------------------------- 替换并重启
say "远端替换并重启"
ssh_do "REMOTE_DIR=$(printf '%q' "${REMOTE_DIR}") WAR_NAME=$(printf '%q' "${WAR_NAME}") UPLOAD_NAME=$(printf '%q' "${UPLOAD_NAME}") bash -s" <<'REMOTE' || die "远端替换或重启失败，如需回滚：bash deploy.sh ${PORT} --rollback"
set -euo pipefail
cd "$REMOTE_DIR"
[ -f "$UPLOAD_NAME" ] || { echo "远端找不到上传包 ${UPLOAD_NAME}" >&2; exit 1; }

rm -f "${WAR_NAME}.bak"
if [ -f "$WAR_NAME" ]; then mv "$WAR_NAME" "${WAR_NAME}.bak"; fi
mv "$UPLOAD_NAME" "$WAR_NAME"
rm -f .upload-*.war
echo "已替换 ${REMOTE_DIR}/${WAR_NAME}"

./start.sh restart
REMOTE

# ---------------------------------------------------------------- 校验
say "健康检查（war 启动需要时间，最多等待 $((HEALTH_TRIES * HEALTH_INTERVAL))s）"
if ! remote_wait_healthy; then
  die "服务未通过健康检查，建议回滚：bash deploy.sh ${PORT} --rollback"
fi

say "发布完成：前端 ${PORT} → 后端 ${HOST}:${REMOTE_DIR}/${WAR_NAME}（${HEALTH_URL} 正常）"
check_frontend
echo "回滚命令：bash deploy.sh ${PORT} --rollback"
