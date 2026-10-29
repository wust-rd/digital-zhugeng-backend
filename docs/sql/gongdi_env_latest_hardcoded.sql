-- =====================================================================
-- 工地环境监测（噪声 / 温度 / 湿度 / 扬尘）最新值 —— 纯硬编码 SQL
-- =====================================================================
-- 用途：
--   1) 在 sqlark / 达梦客户端里直接执行，拿到"标准答案"，用来对照
--      /js/api/env/latest 接口返回的 11 条数据是否正确；
--   2) 作为"硬编码表名"实现的参考样本 —— 10 张分表写在 UNION ALL 里，
--      没有任何动态表名、没有 ${}、不依赖 Java 参数。
--
-- 数据源：ZHGD_2（只读账号 read_all，本脚本全部为 SELECT）
--
-- 【与代码的关系】
--   本脚本 = EnvMonitorDao.xml 里 findLatestRecordsUnionAll 的同口径版本（同为 10 段
--   UNION ALL、同为按 RECORD_TIME 取每设备最新），因此可以直接拿来核对接口返回：
--     GET /js/api/env/latest?sqlMode=hardcoded   本脚本结果 = 该接口的 data 数组
--   唯一区别：本脚本额外 JOIN 了 T_ENGINEERING / T_ACCESS_DEVICE，一次带出项目名与经纬度。
--
-- 【重要】离线执行时不要当成接口实现来看待性能。
--   接口 dynamic 模式现在是"按主键 ENVIRONMENTAL_RECORD_ID 倒序直接取 1 条"
--   （配置 gongdi-env.candidate-limit=1），单台设备 10ms 级、接口整体 0.5s 级；
--   而这里的写法（全表窗口排序）没有索引支撑时在大表上会到秒级，
--   165 万行的分表尤其明显。
--   两者结果一致：2026-10-29 18:08 用同一连接做同时序比对，11 台设备
--   纯主键法与 MAX(RECORD_TIME) 法取到的是同一条记录（SAME=11, DIFF=0）。
-- =====================================================================


-- ---------------------------------------------------------------------
-- 一、一条 SQL 出全部结果（推荐：直接对照接口返回）
--    每行 = 一台环境监测设备的最新一条记录
-- ---------------------------------------------------------------------
WITH LATEST AS (

	-- 项目 635320
	SELECT 635320 AS PROJECT_ID, T.ACCESS_DEVICE_ID, T.NOISE, T.TEMP, T.HUMID, T.TSP, T.RECORD_TIME
	FROM (
		SELECT A.*, ROW_NUMBER() OVER (PARTITION BY A.ACCESS_DEVICE_ID
		                               ORDER BY A.RECORD_TIME DESC) AS RN
		FROM Z_ENVIRONMENTAL_RECORD_635320 A
		WHERE A.IS_DELETE = 0 AND A.RECORD_TIME IS NOT NULL
	) T
	WHERE T.RN = 1

	UNION ALL

	-- 项目 644423
	SELECT 644423 AS PROJECT_ID, T.ACCESS_DEVICE_ID, T.NOISE, T.TEMP, T.HUMID, T.TSP, T.RECORD_TIME
	FROM (
		SELECT A.*, ROW_NUMBER() OVER (PARTITION BY A.ACCESS_DEVICE_ID
		                               ORDER BY A.RECORD_TIME DESC) AS RN
		FROM Z_ENVIRONMENTAL_RECORD_644423 A
		WHERE A.IS_DELETE = 0 AND A.RECORD_TIME IS NOT NULL
	) T
	WHERE T.RN = 1

	UNION ALL

	-- 项目 632474（设备最后一笔数据停在 2024-04-25，属数据断流，非查询问题）
	SELECT 632474 AS PROJECT_ID, T.ACCESS_DEVICE_ID, T.NOISE, T.TEMP, T.HUMID, T.TSP, T.RECORD_TIME
	FROM (
		SELECT A.*, ROW_NUMBER() OVER (PARTITION BY A.ACCESS_DEVICE_ID
		                               ORDER BY A.RECORD_TIME DESC) AS RN
		FROM Z_ENVIRONMENTAL_RECORD_632474 A
		WHERE A.IS_DELETE = 0 AND A.RECORD_TIME IS NOT NULL
	) T
	WHERE T.RN = 1

	UNION ALL

	-- 项目 636613
	SELECT 636613 AS PROJECT_ID, T.ACCESS_DEVICE_ID, T.NOISE, T.TEMP, T.HUMID, T.TSP, T.RECORD_TIME
	FROM (
		SELECT A.*, ROW_NUMBER() OVER (PARTITION BY A.ACCESS_DEVICE_ID
		                               ORDER BY A.RECORD_TIME DESC) AS RN
		FROM Z_ENVIRONMENTAL_RECORD_636613 A
		WHERE A.IS_DELETE = 0 AND A.RECORD_TIME IS NOT NULL
	) T
	WHERE T.RN = 1

	UNION ALL

	-- 项目 650154
	SELECT 650154 AS PROJECT_ID, T.ACCESS_DEVICE_ID, T.NOISE, T.TEMP, T.HUMID, T.TSP, T.RECORD_TIME
	FROM (
		SELECT A.*, ROW_NUMBER() OVER (PARTITION BY A.ACCESS_DEVICE_ID
		                               ORDER BY A.RECORD_TIME DESC) AS RN
		FROM Z_ENVIRONMENTAL_RECORD_650154 A
		WHERE A.IS_DELETE = 0 AND A.RECORD_TIME IS NOT NULL
	) T
	WHERE T.RN = 1

	UNION ALL

	-- 项目 636685
	SELECT 636685 AS PROJECT_ID, T.ACCESS_DEVICE_ID, T.NOISE, T.TEMP, T.HUMID, T.TSP, T.RECORD_TIME
	FROM (
		SELECT A.*, ROW_NUMBER() OVER (PARTITION BY A.ACCESS_DEVICE_ID
		                               ORDER BY A.RECORD_TIME DESC) AS RN
		FROM Z_ENVIRONMENTAL_RECORD_636685 A
		WHERE A.IS_DELETE = 0 AND A.RECORD_TIME IS NOT NULL
	) T
	WHERE T.RN = 1

	UNION ALL

	-- 项目 635686（设备最后一笔数据停在 2026-03-16）
	SELECT 635686 AS PROJECT_ID, T.ACCESS_DEVICE_ID, T.NOISE, T.TEMP, T.HUMID, T.TSP, T.RECORD_TIME
	FROM (
		SELECT A.*, ROW_NUMBER() OVER (PARTITION BY A.ACCESS_DEVICE_ID
		                               ORDER BY A.RECORD_TIME DESC) AS RN
		FROM Z_ENVIRONMENTAL_RECORD_635686 A
		WHERE A.IS_DELETE = 0 AND A.RECORD_TIME IS NOT NULL
	) T
	WHERE T.RN = 1

	UNION ALL

	-- 项目 632472（本项目有 2 台环境监测设备，会返回 2 行）
	SELECT 632472 AS PROJECT_ID, T.ACCESS_DEVICE_ID, T.NOISE, T.TEMP, T.HUMID, T.TSP, T.RECORD_TIME
	FROM (
		SELECT A.*, ROW_NUMBER() OVER (PARTITION BY A.ACCESS_DEVICE_ID
		                               ORDER BY A.RECORD_TIME DESC) AS RN
		FROM Z_ENVIRONMENTAL_RECORD_632472 A
		WHERE A.IS_DELETE = 0 AND A.RECORD_TIME IS NOT NULL
	) T
	WHERE T.RN = 1

	UNION ALL

	-- 项目 636927
	SELECT 636927 AS PROJECT_ID, T.ACCESS_DEVICE_ID, T.NOISE, T.TEMP, T.HUMID, T.TSP, T.RECORD_TIME
	FROM (
		SELECT A.*, ROW_NUMBER() OVER (PARTITION BY A.ACCESS_DEVICE_ID
		                               ORDER BY A.RECORD_TIME DESC) AS RN
		FROM Z_ENVIRONMENTAL_RECORD_636927 A
		WHERE A.IS_DELETE = 0 AND A.RECORD_TIME IS NOT NULL
	) T
	WHERE T.RN = 1

	UNION ALL

	-- 项目 635418（设备最后一笔数据停在 2024-05-10）
	SELECT 635418 AS PROJECT_ID, T.ACCESS_DEVICE_ID, T.NOISE, T.TEMP, T.HUMID, T.TSP, T.RECORD_TIME
	FROM (
		SELECT A.*, ROW_NUMBER() OVER (PARTITION BY A.ACCESS_DEVICE_ID
		                               ORDER BY A.RECORD_TIME DESC) AS RN
		FROM Z_ENVIRONMENTAL_RECORD_635418 A
		WHERE A.IS_DELETE = 0 AND A.RECORD_TIME IS NOT NULL
	) T
	WHERE T.RN = 1

)
SELECT e.ENGINEERING_ID                          AS "项目ID",
       e.NAME                                    AS "项目名称",
       e.LNG                                     AS "经度",
       e.LAT                                     AS "纬度",
       d.ACCESS_DEVICE_ID                        AS "设备ID",
       d.DEVICE_NAME                             AS "设备名称",
       d.MONITOR_PLACE                           AS "监测部位",
       l.NOISE                                   AS "噪声",
       l.TEMP                                    AS "温度",
       l.HUMID                                   AS "湿度",
       l.TSP                                     AS "扬尘",
       TO_CHAR(l.RECORD_TIME, 'YYYY-MM-DD HH24:MI:SS') AS "采集时间"
FROM LATEST l
INNER JOIN T_ENGINEERING e ON e.ENGINEERING_ID = l.PROJECT_ID AND e.IS_DELETE = 0
INNER JOIN T_ACCESS_DEVICE d ON d.ACCESS_DEVICE_ID = l.ACCESS_DEVICE_ID AND d.IS_DELETE = 0
WHERE d.DEVICE_TYPE = 1
ORDER BY e.ENGINEERING_ID, d.ACCESS_DEVICE_ID;


-- ---------------------------------------------------------------------
-- 二、单台设备版（排查某台设备"为什么没数据"时用）
--    把表名和 ACCESS_DEVICE_ID 换成目标值即可；表名是常量，不参与拼接。
--    下面以项目 632472 的表为例，全表只取该设备最新一条。
-- ---------------------------------------------------------------------
-- SELECT ACCESS_DEVICE_ID, NOISE, TEMP, HUMID, TSP,
--        TO_CHAR(RECORD_TIME, 'YYYY-MM-DD HH24:MI:SS') AS RECORD_TIME
-- FROM (
--     SELECT ACCESS_DEVICE_ID, NOISE, TEMP, HUMID, TSP, RECORD_TIME
--     FROM Z_ENVIRONMENTAL_RECORD_632472
--     WHERE ACCESS_DEVICE_ID = /* 设备ID */
--       AND IS_DELETE = 0
--       AND RECORD_TIME IS NOT NULL
--     ORDER BY ENVIRONMENTAL_RECORD_ID DESC
--     FETCH FIRST 200 ROWS ONLY
-- ) T
-- ORDER BY T.RECORD_TIME DESC
-- FETCH FIRST 1 ROWS ONLY;


-- ---------------------------------------------------------------------
-- 三、"设备 vs 明细表"对账（有设备但表里查不到数据时用）
-- ---------------------------------------------------------------------
-- SELECT d.ENGINEERING_ID, d.ACCESS_DEVICE_ID, d.DEVICE_NAME, d.IS_DELETE,
--        (SELECT COUNT(*) FROM Z_ENVIRONMENTAL_RECORD_632472 r
--          WHERE r.ACCESS_DEVICE_ID = d.ACCESS_DEVICE_ID) AS RECORD_CNT
-- FROM T_ACCESS_DEVICE d
-- WHERE d.DEVICE_TYPE = 1
--   AND d.ENGINEERING_ID = 632472
-- ORDER BY d.ACCESS_DEVICE_ID;
