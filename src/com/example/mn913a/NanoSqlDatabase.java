package com.example.mn913a;

import java.math.BigDecimal;

import com.example.mn913a.NanoActivity.DNA_measure_data;
import com.example.mn913a.NanoActivity.Protein_measure_data;

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
	
	public static final int MEASURE_MODE_DNA = 0x01;
	public static final int MEASURE_MODE_PROTEIN = 0x02;

	
	public NanoSqlDatabase ( Context parent ) {
		this.context = parent;
	}
	
	public SQLiteDatabase get_database() {
		return Nano_db;
	}
	
	public void CreateDataDB ( int measure_mode ) {
		Nano_db = SQLiteDatabase.create( null );
		Log.d("DB Path", Nano_db.getPath());
		String amount = String.valueOf(context.databaseList().length);
		Log.d("DB amount", amount);
		
		if ( measure_mode == MEASURE_MODE_DNA ) {
		String sql_dna_value = "CREATE TABLE " + DNA_VALUE_TABLE_NAME + " (" + 
		        INDEX	+ " text not null, " + CONC + " text not null," + A260 + " text not null," +
		        A260_A230	+ " text not null, " + A260_A280 + " text not null "+");";
		try {
			Nano_db.execSQL("DROP TABLE IF EXISTS " + DNA_VALUE_TABLE_NAME);
			Nano_db.execSQL( sql_dna_value );
		} catch (SQLException e) {}
		}
		else 
			if ( measure_mode == MEASURE_MODE_PROTEIN ) {
		
		String sql_protein_value = "CREATE TABLE " + PROTEIN_VALUE_TABLE_NAME + " (" + 
		        INDEX	+ " text not null, " +  A280 + " text not null "+");";
		 //+  PAD_EMPTY + " text not null, " +  PAD_EMPTY + " text not null, " +  PAD_EMPTY + " text not null "
		try {
			Nano_db.execSQL( "DROP TABLE IF EXISTS " + PROTEIN_VALUE_TABLE_NAME );
			Nano_db.execSQL( sql_protein_value );
		} catch (SQLException e) {}
		}
	}
	
	public void InsertDNADataToDB ( DNA_measure_data dna_data ) {
		String index = Integer.toString( dna_data.index );
		String concentration = Double.toString( truncateDecimal ( dna_data.Conc, 3 ).doubleValue() );
		String[] dna_od_array = { Double.toString( truncateDecimal ( dna_data.A260, 3 ).doubleValue() ), Double.toString( truncateDecimal ( dna_data.A260 / dna_data.A230, 3 ).doubleValue() ), Double.toString( truncateDecimal ( dna_data.A260 / dna_data.A280, 3 ).doubleValue() ) };

		String sql_dna_value = "insert into " + DNA_VALUE_TABLE_NAME + " ("
				+ INDEX + ", " + CONC + ", " + A260 + ", " + A260_A230 + ", " + A260_A280 + ") values('" + index + "', '" + concentration
				+ "','" + dna_od_array[0] + "','" + dna_od_array[1] + "','" + dna_od_array[2] + "');";

		try {
			Nano_db.execSQL( sql_dna_value );
		} catch (SQLException e) {
		}
	}
	
	static BigDecimal truncateDecimal(final double x, final int numberofDecimals) {
	    return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_DOWN);
	}
	
	public void InsertPROTEINDataToDB( Protein_measure_data protein_data ) {
		String index = Integer.toString( protein_data.index );
		String A280_value = Double.toString( truncateDecimal ( protein_data.A280, 3 ).doubleValue() );
		
		String sql_protein_value = "insert into " + PROTEIN_VALUE_TABLE_NAME + " ("
				+ INDEX + ", " + A280 + ") values('" + index + "', '" + A280_value + "');";

		try {
			Nano_db.execSQL( sql_protein_value );
		} catch (SQLException e) {
		}
	}
}
