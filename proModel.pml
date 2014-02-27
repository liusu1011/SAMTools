#define Bound_ConPurse 0
#define Bound_LogBook 0
#define Bound_msg_out 0
#define Bound_msg_in 0

typedef type_ConPurse {
  short ConPurse_field1;
  int ConPurse_field2;
  short ConPurse_field3;
  int ConPurse_field4;
  short ConPurse_field5;
  short ConPurse_field6;
  int ConPurse_field7;
  int ConPurse_field8;
  int ConPurse_field9;
  short ConPurse_field10;
  short ConPurse_field11;
  short ConPurse_field12;
  int ConPurse_field13;
  int ConPurse_field14;
  int ConPurse_field15
};

typedef type_LogBook {
  short LogBook_field1;
  short LogBook_field2;
  short LogBook_field3;
  int LogBook_field4;
  int LogBook_field5;
  int LogBook_field6
};

typedef type_msg_out {
  short msg_out_field1;
  short msg_out_field2;
  int msg_out_field3;
  int msg_out_field4;
  short msg_out_field5;
  short msg_out_field6;
  int msg_out_field7;
  int msg_out_field8;
  int msg_out_field9;
  short msg_out_field10
};

typedef type_msg_in {
  short msg_in_field1;
  short msg_in_field2;
  int msg_in_field3;
  int msg_in_field4;
  short msg_in_field5;
  short msg_in_field6;
  int msg_in_field7;
  int msg_in_field8;
  int msg_in_field9;
  short msg_in_field10
};

chan place_ConPurse = [Bound_ConPurse] of {type_ConPurse};
chan place_LogBook = [Bound_LogBook] of {type_LogBook};
chan place_msg_out = [Bound_msg_out] of {type_msg_out};
chan place_msg_in = [Bound_msg_in] of {type_msg_in};

inline pick(var, place_chan, msg){
	var = 1;
	select(var:1..len(place_chan));
	do
	::(var > 1) -> place_chan?msg; place_chan!msg; var--
	::(var == 1) -> break
	od
}
inline is_enabled_startFrom() {
 &&  place_msg_in?[msg_in]
	->
  place_msg_in?msg_in;
	if
	:: atomic{msg_in.msg_in_field1 == startFrom && true && true}
		->startFrom_is_enabled = true
	:: else -> {;
		place_msg_in!msg_in}
	fi
}
inline is_enabled_req() {
 &&  place_msg_in?[msg_in]
	->
  place_msg_in?msg_in;
	if
	:: atomic{msg_in.msg_in_field1 == req && true && true}
		->req_is_enabled = true
	:: else -> {;
		place_msg_in!msg_in}
	fi
}
inline is_enabled_startTo() {
 &&  place_msg_in?[msg_in]
	->
  place_msg_in?msg_in;
	if
	:: atomic{msg_in.msg_in_field1 == startTo && true && true}
		->startTo_is_enabled = true
	:: else -> {;
		place_msg_in!msg_in}
	fi
}
inline is_enabled_readExceptionLog() {
 &&  place_msg_in?[msg_in]
	->
  place_msg_in?msg_in;
	if
	:: atomic{msg_in.msg_in_field1 == readExceptionLog && true}
		->readExceptionLog_is_enabled = true
	:: else -> {;
		place_msg_in!msg_in}
	fi
}
inline is_enabled_clearExceptionLog() {
 &&  place_msg_in?[msg_in]
	->
  place_msg_in?msg_in;
	if
	:: atomic{msg_in.msg_in_field1 == clearExceptionLog && true && true}
		->clearExceptionLog_is_enabled = true
	:: else -> {;
		place_msg_in!msg_in}
	fi
}
inline is_enabled_ack() {
 &&  place_msg_in?[msg_in]
	->
  place_msg_in?msg_in;
	if
	:: atomic{msg_in.msg_in_field1 == ack && true && true}
		->ack_is_enabled = true
	:: else -> {;
		place_msg_in!msg_in}
	fi
}
inline is_enabled_val() {
 &&  place_msg_in?[msg_in]
	->
  place_msg_in?msg_in;
	if
	:: atomic{msg_in.msg_in_field1 == val && true && true}
		->val_is_enabled = true
	:: else -> {;
		place_msg_in!msg_in}
	fi
}
inline is_enabled_Abort() {
 &&  place_msg_in?[msg_in]
	->
  place_msg_in?msg_in;
	if
	:: atomic{((msg_in.msg_in_field1 == startFrom || msg_in.msg_in_field1 == startTo) || msg_in.msg_in_field1 == clearExceptionLog) && true && LogBook.LogBook_field1 == }
		->Abort_is_enabled = true
	:: else -> {;
		place_msg_in!msg_in}
	fi
}
inline is_enabled_readExceptionLogForged() {
 &&  place_msg_in?[msg_in]
	->
  place_msg_in?msg_in;
	if
	:: atomic{msg_in.msg_in_field1 == readExceptionLog && true}
		->readExceptionLogForged_is_enabled = true
	:: else -> {;
		place_msg_in!msg_in}
	fi
}
inline is_enabled_ether() {
  place_msg_out?[msg_out]
	->
  place_msg_out?msg_out;
	if
	:: atomic{e_in != forged && true}
		->ether_is_enabled = true
	:: else -> { place_msg_out!msg_out}
	fi
}
inline fire_startFrom() {
  msg_out.msg_out_field1 = 	ConPurse.ConPurse_field1 = forged;
;
  place_msg_out!msg_out;
  startFrom_is_enabled = false
}
inline fire_req() {
  msg_out.msg_out_field1 = 	ConPurse.ConPurse_field1 = val;
	ConPurse.ConPurse_field5 = ConPurse.ConPurse_field5;
	ConPurse.ConPurse_field6 = ConPurse.ConPurse_field6;
	ConPurse.ConPurse_field7 = ConPurse.ConPurse_field7;
	ConPurse.ConPurse_field8 = ConPurse.ConPurse_field8;
	ConPurse.ConPurse_field9 = ConPurse.ConPurse_field9;
;
  place_msg_out!msg_out;
  req_is_enabled = false
}
inline fire_startTo() {
  msg_out.msg_out_field1 = 	ConPurse.ConPurse_field1 = req;
	ConPurse.ConPurse_field6 = ConPurse.ConPurse_field1;
	ConPurse.ConPurse_field8 = ConPurse.ConPurse_field4;
;
  place_msg_out!msg_out;
  startTo_is_enabled = false
}
inline fire_readExceptionLog() {
  place_msg_out!msg_out;
  readExceptionLog_is_enabled = false
}
inline fire_clearExceptionLog() {
  msg_out.msg_out_field1 = 	ConPurse.ConPurse_field1 = forged;
;
  place_msg_out!msg_out;
  clearExceptionLog_is_enabled = false
}
inline fire_ack() {
  msg_out.msg_out_field1 = 	ConPurse.ConPurse_field1 = forged;
;
  place_msg_out!msg_out;
  ack_is_enabled = false
}
inline fire_val() {
  msg_out.msg_out_field1 = 	ConPurse.ConPurse_field1 = ack;
	ConPurse.ConPurse_field5 = ConPurse.ConPurse_field5;
	ConPurse.ConPurse_field6 = ConPurse.ConPurse_field6;
	ConPurse.ConPurse_field7 = ConPurse.ConPurse_field7;
	ConPurse.ConPurse_field8 = ConPurse.ConPurse_field8;
	ConPurse.ConPurse_field9 = ConPurse.ConPurse_field9;
;
  place_msg_out!msg_out;
  val_is_enabled = false
}
inline fire_Abort() {
  Abort_is_enabled = false
}
inline fire_readExceptionLogForged() {
  place_msg_out!msg_out;
  readExceptionLogForged_is_enabled = false
}
inline fire_ether() {
  msg_in.msg_in_field1 = msg_out.msg_out_field1;
  msg_in.msg_in_field2 = msg_out.msg_out_field2;
  msg_in.msg_in_field3 = msg_out.msg_out_field3;
  msg_in.msg_in_field4 = msg_out.msg_out_field4;
  msg_in.msg_in_field5 = msg_out.msg_out_field5;
  msg_in.msg_in_field6 = msg_out.msg_out_field6;
  msg_in.msg_in_field7 = msg_out.msg_out_field7;
  msg_in.msg_in_field8 = msg_out.msg_out_field8;
  msg_in.msg_in_field9 = msg_out.msg_out_field9;
  msg_in.msg_in_field10 = msg_out.msg_out_field10;
  place_msg_in!msg_in;
  ether_is_enabled = false
}
inline startFrom() {
  is_enabled_startFrom();
  if
  ::  startFrom_is_enabled -> atomic{fire_startFrom()}
  ::  else -> skip
  fi
}
inline req() {
  is_enabled_req();
  if
  ::  req_is_enabled -> atomic{fire_req()}
  ::  else -> skip
  fi
}
inline startTo() {
  is_enabled_startTo();
  if
  ::  startTo_is_enabled -> atomic{fire_startTo()}
  ::  else -> skip
  fi
}
inline readExceptionLog() {
  is_enabled_readExceptionLog();
  if
  ::  readExceptionLog_is_enabled -> atomic{fire_readExceptionLog()}
  ::  else -> skip
  fi
}
inline clearExceptionLog() {
  is_enabled_clearExceptionLog();
  if
  ::  clearExceptionLog_is_enabled -> atomic{fire_clearExceptionLog()}
  ::  else -> skip
  fi
}
inline ack() {
  is_enabled_ack();
  if
  ::  ack_is_enabled -> atomic{fire_ack()}
  ::  else -> skip
  fi
}
inline val() {
  is_enabled_val();
  if
  ::  val_is_enabled -> atomic{fire_val()}
  ::  else -> skip
  fi
}
inline Abort() {
  is_enabled_Abort();
  if
  ::  Abort_is_enabled -> atomic{fire_Abort()}
  ::  else -> skip
  fi
}
inline readExceptionLogForged() {
  is_enabled_readExceptionLogForged();
  if
  ::  readExceptionLogForged_is_enabled -> atomic{fire_readExceptionLogForged()}
  ::  else -> skip
  fi
}
inline ether() {
  is_enabled_ether();
  if
  ::  ether_is_enabled -> atomic{fire_ether()}
  ::  else -> skip
  fi
}
proctype Main() {
  bool startFrom_is_enabled = false;
  bool req_is_enabled = false;
  bool startTo_is_enabled = false;
  bool readExceptionLog_is_enabled = false;
  bool clearExceptionLog_is_enabled = false;
  bool ack_is_enabled = false;
  bool val_is_enabled = false;
  bool Abort_is_enabled = false;
  bool readExceptionLogForged_is_enabled = false;
  bool ether_is_enabled = false;
  type_ConPurse ConPurse;
	int var_ConPurse=1;
  type_LogBook LogBook;
	int var_LogBook=1;
  type_msg_out msg_out;
  type_msg_in msg_in;

  do
  :: atomic{ startFrom() }
  :: atomic{ req() }
  :: atomic{ startTo() }
  :: atomic{ readExceptionLog() }
  :: atomic{ clearExceptionLog() }
  :: atomic{ ack() }
  :: atomic{ val() }
  :: atomic{ Abort() }
  :: atomic{ readExceptionLogForged() }
  :: atomic{ ether() }
  od
}
init {
  type_ConPurse ConPurse;
  ConPurse.ConPurse_field1=bob;
  ConPurse.ConPurse_field2=250;
  ConPurse.ConPurse_field3=true;
  ConPurse.ConPurse_field4=2;
  ConPurse.ConPurse_field5=bob;
  ConPurse.ConPurse_field6=arda;
  ConPurse.ConPurse_field7=50;
  ConPurse.ConPurse_field8=1;
  ConPurse.ConPurse_field9=1;
  ConPurse.ConPurse_field10=idle;
  ConPurse.ConPurse_field11=none;
  ConPurse.ConPurse_field12=none;
  ConPurse.ConPurse_field13=0;
  ConPurse.ConPurse_field14=1;
  ConPurse.ConPurse_field15=1;
  place_ConPurse!ConPurse;
  ConPurse.ConPurse_field1=arda;
  ConPurse.ConPurse_field2=50;
  ConPurse.ConPurse_field3=true;
  ConPurse.ConPurse_field4=2;
  ConPurse.ConPurse_field5=arda;
  ConPurse.ConPurse_field6=bob;
  ConPurse.ConPurse_field7=50;
  ConPurse.ConPurse_field8=1;
  ConPurse.ConPurse_field9=1;
  ConPurse.ConPurse_field10=idle;
  ConPurse.ConPurse_field11=none;
  ConPurse.ConPurse_field12=none;
  ConPurse.ConPurse_field13=0;
  ConPurse.ConPurse_field14=1;
  ConPurse.ConPurse_field15=1;
  place_ConPurse!ConPurse;
  type_LogBook LogBook;
  type_msg_out msg_out;
  msg_out.msg_out_field1=forged;
  msg_out.msg_out_field2=bob;
  msg_out.msg_out_field3=50;
  msg_out.msg_out_field4=1;
  msg_out.msg_out_field5=arda;
  msg_out.msg_out_field6=bob;
  msg_out.msg_out_field7=50;
  msg_out.msg_out_field8=1;
  msg_out.msg_out_field9=1;
  msg_out.msg_out_field10=arda;
  place_msg_out!msg_out;
  msg_out.msg_out_field1=forged;
  msg_out.msg_out_field2=arda;
  msg_out.msg_out_field3=50;
  msg_out.msg_out_field4=1;
  msg_out.msg_out_field5=bob;
  msg_out.msg_out_field6=arda;
  msg_out.msg_out_field7=50;
  msg_out.msg_out_field8=1;
  msg_out.msg_out_field9=1;
  msg_out.msg_out_field10=arda;
  place_msg_out!msg_out;
  type_msg_in msg_in;
run Main()
}
ltl f{}