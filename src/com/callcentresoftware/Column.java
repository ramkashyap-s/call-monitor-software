package com.callcentresoftware;

public enum Column {
	CALL_ID(0),
	EVENT_TYPE(1),
	CALLING_PARTY(2),
	RECEIVING_PARTY(3),
	;

	private final int columnIndex;

	private Column(int columnIndex) {
		this.columnIndex = columnIndex;
	}

	public int getIndex() {
		return columnIndex;
	}
}

