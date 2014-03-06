#define Bound_DataIn 10
#define Bound_Deliver 10
#define Bound_AckOut 10
#define Bound_AckBuf 10
#define Bound_Accept 10
#define Bound_DataBuf 10
#define Bound_DataOut 10
#define Bound_AckIn 10

typedef type_DataIn {
  int DataIn_field1;
  int DataIn_field2
};

typedef type_Deliver {
  int Deliver_field1
};

typedef type_AckOut {
  int AckOut_field1
};

typedef type_AckBuf {
  int AckBuf_field1
};

typedef type_Accept {
  int Accept_field1
};

typedef type_DataBuf {
  int DataBuf_field1;
  int DataBuf_field2
};

typedef type_DataOut {
  int DataOut_field1;
  int DataOut_field2
};

typedef type_AckIn {
  int AckIn_field1
};

chan place_DataIn = [Bound_DataIn] of {type_DataIn};
chan place_Deliver = [Bound_Deliver] of {type_Deliver};
chan place_AckOut = [Bound_AckOut] of {type_AckOut};
chan place_AckBuf = [Bound_AckBuf] of {type_AckBuf};
chan place_Accept = [Bound_Accept] of {type_Accept};
chan place_DataBuf = [Bound_DataBuf] of {type_DataBuf};
chan place_DataOut = [Bound_DataOut] of {type_DataOut};
chan place_AckIn = [Bound_AckIn] of {type_AckIn};

inline pick(var, place_chan, msg){
	var = 1;
	select(var:1..len(place_chan));
	do
	::(var > 1) -> place_chan?msg; place_chan!msg; var--
	::(var == 1) -> break
	od
}
inline is_enabled_deliverData() {
  place_DataIn?[DataIn] &&  place_AckBuf?[AckBuf]
	->
  place_DataIn?DataIn;
  place_AckBuf?AckBuf;
	if
	:: atomic{(DataIn.DataIn_field1 == 0 || DataIn.DataIn_field1 == 1) && DataIn.DataIn_field1 == (AckBuf.AckBuf_field1 + 1) % 2 && true && true && true}
		->deliverData_is_enabled = true
	:: else -> { place_DataIn!DataIn;
		place_AckBuf!AckBuf}
	fi
}
inline is_enabled_resendAck() {
  place_DataIn?[DataIn] &&  place_AckBuf?[AckBuf]
	->
  place_DataIn?DataIn;
  place_AckBuf?AckBuf;
	if
	:: atomic{(DataIn.DataIn_field1 == 3 || DataIn.DataIn_field1 == 4) && true && true}
		->resendAck_is_enabled = true
	:: else -> { place_DataIn!DataIn;
		place_AckBuf!AckBuf}
	fi
}
inline is_enabled_sendData() {
  place_Accept?[Accept] &&  place_DataBuf?[DataBuf] &&  place_AckIn?[AckIn]
	->
  place_Accept?Accept;
  place_DataBuf?DataBuf;
  place_AckIn?AckIn;
	if
	:: atomic{(AckIn.AckIn_field1 == 0 || AckIn.AckIn_field1 == 1) && AckIn.AckIn_field1 == DataBuf.DataBuf_field1 && true && true && true && true}
		->sendData_is_enabled = true
	:: else -> { place_Accept!Accept;
		place_DataBuf!DataBuf;
		place_AckIn!AckIn}
	fi
}
inline is_enabled_resendData() {
  place_DataBuf?[DataBuf] &&  place_AckIn?[AckIn]
	->
  place_DataBuf?DataBuf;
  place_AckIn?AckIn;
	if
	:: atomic{(AckIn.AckIn_field1 == 3 || AckIn.AckIn_field1 == 4) && true && true && true && true}
		->resendData_is_enabled = true
	:: else -> { place_DataBuf!DataBuf;
		place_AckIn!AckIn}
	fi
}
inline is_enabled_Conn5() {
  place_AckOut?[AckOut]
	->
  place_AckOut?AckOut;
	if
	:: true ->Conn5_is_enabled = true
	:: else -> { place_AckOut!AckOut}
	fi
}
inline is_enabled_Conn4() {
  place_CAckIn?[CAckIn]
	->
  place_CAckIn?CAckIn;
	if
	:: true ->Conn4_is_enabled = true
	:: else -> { place_CAckIn!CAckIn}
	fi
}
inline is_enabled_Conn3() {
  place_CDataIn?[CDataIn]
	->
  place_CDataIn?CDataIn;
	if
	:: true ->Conn3_is_enabled = true
	:: else -> { place_CDataIn!CDataIn}
	fi
}
inline is_enabled_Conn2() {
  place_DataOut?[DataOut]
	->
  place_DataOut?DataOut;
	if
	:: true ->Conn2_is_enabled = true
	:: else -> { place_DataOut!DataOut}
	fi
}
inline fire_deliverData() {
  Deliver.Deliver_field1 = DataIn.DataIn_field2;
  AckOut.AckOut_field1 = DataIn.DataIn_field1;
  AckBuf.AckBuf_field1 = DataIn.DataIn_field1;
  place_Deliver!Deliver;
  place_AckOut!AckOut;
  place_AckBuf!AckBuf;
  deliverData_is_enabled = false
}
inline fire_resendAck() {
  AckBuf.AckBuf_field1 = AckBuf.AckBuf_field1;
  AckOut.AckOut_field1 = AckBuf.AckBuf_field1;
  place_AckOut!AckOut;
  place_AckBuf!AckBuf;
  resendAck_is_enabled = false
}
inline fire_sendData() {
		DataOut.DataOut_field1 = (AckIn.AckIn_field1 + 1) % 2;
		DataOut.DataOut_field2=Accept.Accept_field1;
		DataBuf.DataBuf_field1 = (AckIn.AckIn_field1 + 1) % 2;
		DataBuf.DataBuf_field2=Accept.Accept_field1;
  place_DataBuf!DataBuf;
  place_DataOut!DataOut;
  sendData_is_enabled = false
}
inline fire_resendData() {
		DataOut.DataOut_field1 = DataBuf.DataBuf_field1;
		DataOut.DataOut_field2 = DataBuf.DataBuf_field2;
		DataBuf.DataBuf_field1 = DataBuf.DataBuf_field1;
		DataBuf.DataBuf_field2 = DataBuf.DataBuf_field2;
  place_DataBuf!DataBuf;
  place_DataOut!DataOut;
  resendData_is_enabled = false
}
inline fire_Conn5() {
  CAckOut.CAckOut_field1 = AckOut.AckOut_field1;
  place_CAckOut!CAckOut;
  Conn5_is_enabled = false
}
inline fire_Conn4() {
  AckIn.AckIn_field1 = CAckIn.CAckIn_field1;
  place_AckIn!AckIn;
  Conn4_is_enabled = false
}
inline fire_Conn3() {
  DataIn.DataIn_field1 = CDataIn.CDataIn_field1;
  DataIn.DataIn_field2 = CDataIn.CDataIn_field2;
  place_DataIn!DataIn;
  Conn3_is_enabled = false
}
inline fire_Conn2() {
  CDataOut.CDataOut_field1 = DataOut.DataOut_field1;
  CDataOut.CDataOut_field2 = DataOut.DataOut_field2;
  place_CDataOut!CDataOut;
  Conn2_is_enabled = false
}
inline deliverData() {
  is_enabled_deliverData();
  if
  ::  deliverData_is_enabled -> atomic{fire_deliverData()}
  ::  else -> skip
  fi
}
inline resendAck() {
  is_enabled_resendAck();
  if
  ::  resendAck_is_enabled -> atomic{fire_resendAck()}
  ::  else -> skip
  fi
}
inline sendData() {
  is_enabled_sendData();
  if
  ::  sendData_is_enabled -> atomic{fire_sendData()}
  ::  else -> skip
  fi
}
inline resendData() {
  is_enabled_resendData();
  if
  ::  resendData_is_enabled -> atomic{fire_resendData()}
  ::  else -> skip
  fi
}
inline Conn5() {
  is_enabled_Conn5();
  if
  ::  Conn5_is_enabled -> atomic{fire_Conn5()}
  ::  else -> skip
  fi
}
inline Conn4() {
  is_enabled_Conn4();
  if
  ::  Conn4_is_enabled -> atomic{fire_Conn4()}
  ::  else -> skip
  fi
}
inline Conn3() {
  is_enabled_Conn3();
  if
  ::  Conn3_is_enabled -> atomic{fire_Conn3()}
  ::  else -> skip
  fi
}
inline Conn2() {
  is_enabled_Conn2();
  if
  ::  Conn2_is_enabled -> atomic{fire_Conn2()}
  ::  else -> skip
  fi
}
proctype Main() {
  bool deliverData_is_enabled = false;
  bool resendAck_is_enabled = false;
  bool sendData_is_enabled = false;
  bool resendData_is_enabled = false;
  bool Conn5_is_enabled = false;
  bool Conn4_is_enabled = false;
  bool Conn3_is_enabled = false;
  bool Conn2_is_enabled = false;
  type_DataIn DataIn;
  type_Deliver Deliver;
  type_AckOut AckOut;
  type_AckBuf AckBuf;
  type_Accept Accept;
  type_DataBuf DataBuf;
  type_DataOut DataOut;
  type_AckIn AckIn;

  do
  :: atomic{ deliverData() }
  :: atomic{ resendAck() }
  :: atomic{ sendData() }
  :: atomic{ resendData() }
  :: atomic{ Conn5() }
  :: atomic{ Conn4() }
  :: atomic{ Conn3() }
  :: atomic{ Conn2() }
  od
}
init {
  type_DataIn DataIn;
  type_Deliver Deliver;
  type_AckOut AckOut;
  type_AckBuf AckBuf;
  AckBuf.AckBuf_field1=1;
  place_AckBuf!AckBuf;
  type_Accept Accept;
  Accept.Accept_field1=1;
  place_Accept!Accept;
  Accept.Accept_field1=2;
  place_Accept!Accept;
  Accept.Accept_field1=3;
  place_Accept!Accept;
  Accept.Accept_field1=4;
  place_Accept!Accept;
  Accept.Accept_field1=5;
  place_Accept!Accept;
  type_DataBuf DataBuf;
  DataBuf.DataBuf_field1=1;
  DataBuf.DataBuf_field2=999;
  place_DataBuf!DataBuf;
  type_DataOut DataOut;
  type_AckIn AckIn;
  AckIn.AckIn_field1=1;
  place_AckIn!AckIn;
run Main()
}
ltl f{??}