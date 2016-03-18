package com.example.mn913a;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class NanoSqlDatabase {
	public static Context context = null;
	SQLiteDatabase Nano_db = null;
	public static final String DNA_VALUE_TABLE_NAME = "dna";
	private static final String INDEX = "NO";
	private static final String CONC = "Conc";
	private static final String A260 = "A260";
	private static final String A260_A230 = "A260_A230_ratio";
	private static final String A260_A280 = "A260_A280_ratio";
	
	public static final String PROTEIN_VALUE_TABLE_NAME = "protein";
	private static final String A280 = "A280";
	private static final String PAD_EMPTY = "";

	
	public NanoSqlDatabase ( Context parent ) {
		this.context = parent;
	}
	
	public SQLiteDatabase get_database() {
		return Nano_db;
	}
	
	public void CreateDataDB ( int total_sensor ) {
		Nano_db = SQLiteDatabase.create( null );
		Log.d("DB Path", Nano_db.getPath());
		String amount = String.valueOf(context.databaseList().length);
		Log.d("DB amount", amount);
		
		String sql_dna_value = "CREATE TABLE " + DNA_VALUE_TABLE_NAME + " (" + 
		        INDEX	+ " text not null, " + CONC + " text not null," + A260 + " text not null," +
		        A260_A230	+ " text not null, " + A260_A280 + " text not null "+");";
		try {
			Nano_db.execSQL("DROP TABLE IF EXISTS " + DNA_VALUE_TABLE_NAME);
			Nano_db.execSQL( sql_dna_value );
		} catch (SQLException e) {}
		
		String sql_protein_value = "CREATE TABLE " + PROTEIN_VALUE_TABLE_NAME + " (" + 
		        INDEX	+ " text not null, " +  A280 + " text not null " +  PAD_EMPTY + " text not null " +  PAD_EMPTY + " text not null " +  PAD_EMPTY + " text not null "
				+ ");";
		try {
			Nano_db.execSQL( "DROP TABLE IF EXISTS " + PROTEIN_VALUE_TABLE_NAME );
			Nano_db.execSQL( sql_protein_value );
		} catch (SQLException e) {}
	}
	
	public void InsertDNADataToDB ( ) {
		String index = "NA";
		String measure_time = "NA";
		String[] dna_od_array = { "NA", "NA", "NA" };

		String sql_dna_value = "insert into " + DNA_VALUE_TABLE_NAME + " ("
				+ INDEX + ", " + CONC + ", " + A260 + ", " + A260_A230 + ", " + A260_A280 + ") values('" + index + "', '" + measure_time
				+ "','" + dna_od_array[0] + "','" + dna_od_array[1] + "','" + dna_od_array[2] + "');";

		try {
			Nano_db.execSQL( sql_dna_value );
		} catch (SQLException e) {
		}
	}
}
