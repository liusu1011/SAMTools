package hlpn2smt;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import formulaParser.ErrorMsg;
import formulaParser.Formula2SMTZ3;
import formulaParser.Parse;
import formulaParser.formulaAbsyntree.Sentence;
import pipe.dataLayer.Arc;
import pipe.dataLayer.BasicType;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.DataType;
import pipe.dataLayer.Place;
import pipe.dataLayer.Token;
import pipe.dataLayer.Transition;

public class RefineZ3Converter extends HLPNModelToZ3Converter{
	DataLayer model;
		
	public RefineZ3Converter(DataLayer _model, int _depth, ArrayList<Property> _prop) {
		super(_model, _depth, _prop);
		
	}
 
}
