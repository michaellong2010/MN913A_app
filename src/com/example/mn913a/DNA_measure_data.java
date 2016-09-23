package com.example.mn913a;

import java.io.Serializable;

public class DNA_measure_data implements Serializable {
	int index;
	double A260, A230, A280, A320, Conc;
	boolean include_A320;
	double OD260, OD230, OD280, OD320;
}
