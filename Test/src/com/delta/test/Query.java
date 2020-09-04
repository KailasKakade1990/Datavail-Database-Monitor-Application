package com.delta.test;

public class Query {

	public static void main(String[] args) {

		// String query = "show Oracle status";
		StringBuilder query = new StringBuilder();

		query.append("SELECT g.name \"Group\", d.name \"Disk\",d.total_mb,d.free_mb,g.disk_same_size FROM v$asm_disk d,(  SELECT ag.group_number,ag.name,DECODE((MIN(ad.total_mb) - MAX (ad.total_mb)),0, 'TRUE','FALSE')disk_same_size FROM v$asm_diskgroup ag,v$asm_disk ad WHERE ad.group_number = ag.group_number GROUP BY ag.group_number, ag.name) g WHERE d.group_number = g.group_number ORDER BY g.name, d.name");
	String ag = query.toString();

		System.out.println(ag);

	}
}
