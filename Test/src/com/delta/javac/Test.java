package com.delta.javac;

import java.io.IOException;

public class Test {

	public static void main(String args[]) throws IOException {

		StringBuilder query = new StringBuilder();

		query.append(
				"SELECT tablespace_name \"TABLESPACE\", ROUND(((alloc_bytes - free_bytes) / alloc_bytes * 100)) \"ALLOC_USAGE\",ROUND(alloc_bytes / 1024 / 1024 / 1024, 2) \"ALLOC_GB\",ROUND(free_bytes / 1024 / 1024 / 1024, 2) \"ALLOC_FREE_GB\",ROUND((alloc_bytes - free_bytes) / 1024 / 1024 / 1024, 2) \"ALLOC_USED_GB\",ROUND(free_bytes / alloc_bytes * 100) \"ALLOC_FREE_PCT\",ROUND(DECODE(max_bytes, 0, alloc_bytes, max_bytes) / 1024 / 1024 / 1024, 2) \"MAX_EXTENDABLE_GB\",ROUND((alloc_bytes - free_bytes) / DECODE(max_bytes, 0, alloc_bytes, max_bytes) * 100) \"EXTENDABLE_USAGE\" FROM( ");
		query.append(
				"SELECT a.tablespace_name tablespace_name, a.bytes_alloc alloc_bytes, NVL (b.bytes_free, 0) free_bytes, max_bytes max_bytes FROM(SELECT f.tablespace_name, SUM(f.bytes) bytes_alloc, SUM(DECODE(f.autoextensible, 'YES', f.maxbytes, 'NO', f.bytes)) max_bytes FROM dba_data_files f GROUP BY tablespace_name) a, (SELECT f.tablespace_name, SUM(f.bytes) bytes_free  FROM dba_free_space f  GROUP BY tablespace_name) b ");
		query.append(
				"WHERE a.tablespace_name = b.tablespace_name(+) UNION ALL SELECT h.tablespace_name tablespace_name, SUM (h.bytes_free + h.bytes_used) alloc_bytes, SUM((h.bytes_free + h.bytes_used) - NVL(p.bytes_used, 0)) free_bytes, SUM(f.maxbytes) max_bytes FROM sys.v_$temp_space_header h, sys.v_$temp_extent_pool p, dba_temp_files f WHERE p.file_id(+) = h.file_id AND p.tablespace_name(+) = h.tablespace_name AND f.file_id = h.file_id AND f.tablespace_name = h.tablespace_name  GROUP BY h.tablespace_name)");
		
		System.out.println("::" + query);
	}
}