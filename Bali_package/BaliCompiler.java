package Bali_package;
import edu.cornell.cs.sam.io.SamTokenizer;
import edu.cornell.cs.sam.io.Tokenizer;
import edu.cornell.cs.sam.io.Tokenizer.TokenType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import Bali_package.symbol_table;
import Bali_package.function_table;

//TODO: make sure the offsets for params are in the right order
//TODO: make sure SP moves properly
//include if else (I only have if at the moment)
public class BaliCompiler
{

	int label_counter = 0;
	LinkedList function_end_label_ll = new LinkedList<String>();
	public String callee_preparation(String formals){
		return "callee_preparation;";
	}
	public String caller_preparation(String formals){
		return "caller preparation;\n";
	}
	public String caller_preparation(){
		return "caller preparation;\n";
	}   



	public String main_preparation(){
		return "main_preparation;";
	}


	public function_table prg_func_table = new function_table();

	public String compiler(String fileName) 
	{

		//returns SaM code for program in file
		try 
		{
			SamTokenizer f = new SamTokenizer (fileName);
			String pgm = getProgram(f);
			System.out.println(pgm);
			return pgm;
		} 
		catch (Exception e) 
		{
			//e.getCause();
			System.out.println(e.getMessage());
			System.out.println("Fatal error: could not compile program");
			return "STOP\n";
		}
	}
	String getProgram(SamTokenizer f)
	{
		//generates the code for the program
		try
		{
			String prg="";
			//main_preparation();	
			prg += "PUSHIMM 0\n";
			prg += "LINK\n";
			prg += "JSR main\n";
			prg += "POPFBR\n";
			prg += "STOP\n";
			while(f.peekAtKind()!=TokenType.EOF) // till EOF, find another method
			{
				prg += getMethod(f) + "\n\n";
			}

			prg += "\n\n\n"; 
			return prg;
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			System.out.println("Fatal error: could not compile program");
			return "STOP\n";
		}		
	}

	//generates SAM code for any method written in Bali
	String getMethod(SamTokenizer f)
	{


		String function_code ="";
		symbol_table method_symbol_table = new symbol_table();

		//method name
		f.match("int");

		if(f.peekAtKind()!=TokenType.WORD)
			System.out.println("ERROR : Function name invalid\n");

		String methodName = f.getWord(); 

		if(function_end_label_ll.contains(methodName) || (methodName.compareTo("int")==0))
			System.out.println("ERROR : Function name exists\n");

		System.out.println(methodName);
		function_end_label_ll.add(methodName);

		label_counter +=1;

		//method formals
		f.match('(');
		String formals = getFormals(f);	     	
		f.match(')');  // must be a closing parenthesis
		System.out.println("formals: " + formals); 
		//System.out.println("End Of Formals"); 

		//fill up the symbol_table with the formals
		String[] formals_splitted= formals.split(" ");
		for (int i=0; i< formals_splitted.length; i++){
			method_symbol_table.add_variable(formals_splitted[i]);
			method_symbol_table.add_params(formals_splitted[i]);
		}

		//callee prepartion 
		//function_code += callee_preparation(formals);

		//body 
		f.match('{');
		String body = getBody(f, method_symbol_table);
		f.match('}');  // must be an closing parenthesis
		//System.out.println("body:\n" + body + "\n"); 
		//System.out.println("End Of body"); 

		function_code += methodName + ":";
		function_code += "ADDSP " + method_symbol_table.get_n_locals() + "\n";
		function_code += body;
		String end_label = "\nfEnd"+methodName;
		function_code += end_label +":";
		function_code += "STOREOFF " + -1*(method_symbol_table.get_n_params() + 1) + "\n"; 
		function_code  += "ADDSP" + -1*(method_symbol_table.get_n_locals()) + "\n";
		function_code += "JUMPIND\n";
		//update function_table

		prg_func_table.add_function(methodName, formals_splitted.length, function_code );


		function_end_label_ll.removeLast();
		return function_code;
	}

	String getBreak(SamTokenizer f, symbol_table method_symbol_table){
		f.match('}');
		//TODO: LAbel here?
		String break_string="";
		return break_string;
	}

	String getWhile(SamTokenizer f, symbol_table method_symbol_table){
		f.match('(');
		String while_predicate = getExp(f, method_symbol_table);
		f.match(')');
		String while_body = getStatement(f, method_symbol_table);  

		String while_begin_label =  "Label" + label_counter;
		label_counter +=1;

		String predicate_true_label =  "Label" + label_counter;
		label_counter +=1;

//TODO: Look at JUMPC to fix this code
		String while_code = "\n" + while_begin_label + ":" + while_predicate; 
		while_code += "JUMPC " +  predicate_true_label + "\n";
		while_code += "\n" + predicate_true_label + ":" + while_body;
		while_code += "JUMP " + while_begin_label + "\n";
		return while_code;
	}

	String getIf(SamTokenizer f, symbol_table method_symbol_table){
		f.match('(');
		String if_predicate = getExp(f, method_symbol_table);
		f.match(')');
		String if_body = getStatement(f, method_symbol_table);  
		f.match("else");
		String else_body = getStatement(f, method_symbol_table);  

		String true_label =  "Label" + label_counter;
		label_counter +=1;
		String end_true_label =  "Label" + label_counter;
		label_counter +=1;
		String if_true = "JUMPC " + true_label + "\n";
		String if_false = "JUMP " + end_true_label + "\n";

		//TODO possibly change to getBody 

		String if_code = if_predicate;
		if_code += if_true;
		if_code += else_body;
		if_code += if_false;
		if_code += true_label +":"; 
		if_code += if_body;
		if_code += end_true_label;

		return if_code;
	}

	String getReturn(SamTokenizer f, symbol_table method_symbol_table){
		String exp = getExp(f, method_symbol_table);  
		f.match(';');

		String return_label = "fEnd"+(String) function_end_label_ll.getLast();

		exp += "JUMP " + return_label + "\n"; 
		return exp;
	}
	//TODO: if then else 



	String getExp(SamTokenizer f, symbol_table method_symbol_table) 
	{
		String exp = ""; 
		switch (f.peekAtKind()) {
			case INTEGER: //E -> integer
				return "PUSHIMM "+f.getInt() + "\n";
				//return "PUSHIMM " + f.getInt() + "\n";
			case OPERATOR:  
				{
					switch(f.getOp()){//TODO make sure all the operators are covered
						case ('('):
							{
								String someExp = getExp(f, method_symbol_table); 
								char nextOp =  f.getOp();
								if (nextOp == '+') {
									exp += someExp;
									exp += getExp(f, method_symbol_table);
									exp += "ADD\n";
									f.match(')'); 
								}else if (nextOp == '-'){
									exp += someExp;
									exp += getExp(f, method_symbol_table);
									exp += "SUB\n";
									f.match(')'); 

								}else if (nextOp == '*'){
									exp += someExp;
									exp += getExp(f, method_symbol_table);
									exp += "TIMES\n";
									f.match(')'); 

								} else if(nextOp == '>'){
									exp += someExp;
									exp += getExp(f, method_symbol_table);
									exp += "CMP\n";
									exp += "ISPOS\n";
									f.match(')'); 
								}else if ( nextOp == '<') {
									exp += someExp;
									exp += getExp(f, method_symbol_table);
									exp += "CMP\n";
									exp += "ISPOS\n";
									f.match(')'); 
								}else if ( nextOp == '=') {
									exp += someExp;
									exp += getExp(f, method_symbol_table);
									exp += "CMP\n";
									exp += "ISNIL\n";
									f.match(')'); 
								}else{
									System.out.println("this operator " + nextOp + " is not defined\n");
								}
								break; 
							}
						default: return "ERROR in getExp OPERATOR\n";
					}
					/*
					   nextOp = (f.getOp());
					   if (nextOp != ';'  && nextOp != ',') {
					   System.out.println("expression needs to end with a ; or ,\n");
					   }     
					 */ 
					return exp; 
				}
			case WORD:  //declarations, blocks, assignements
				{
					String theWord =  f.getWord();
					f.pushBack();

					if (prg_func_table.has_func(theWord)){ //it's a function call
						String function_name = f.getWord(); 


						char Op = f.getOp(); 
						do{
							getExp(f, method_symbol_table); 
							switch(f.peekAtKind()){
								case OPERATOR:
									{ 
										char nextOp = f.getOp();
										if( nextOp == ')') {
											String code = "";
											code += "LINK\n";
											code += "JSR " + function_name + "\n";
											code += "POPFBR\n";
											code += "ADDSP " + -1*prg_func_table.get_function_n_args(function_name) + "\n";
											return code;
										}
									}         
							}
						}while(true);   
					}
					else{ //variable assignment
						f.getWord(); 
						int offSet = 0; 
						if (method_symbol_table.has_param(theWord)) {
							offSet = method_symbol_table.get_param_offset(theWord);
						}else if (method_symbol_table.has_local(theWord)) {
							offSet = method_symbol_table.get_local_offset(theWord);
						}else{
							System.out.println("this variable is not defined\n");
							System.exit(0);
						}
						return  "PUSHOFF " + offSet +"\n";//actually loading 
					}
				}
			default:   return "ERROR\n";
		}
	}


	String getFormals(SamTokenizer f){
		String formals = "";
		int flag = 0;
		do 
		{
			switch(f.peekAtKind()){
				case OPERATOR:
					char current_op =  f.getOp();
					if (current_op == ')') {
						//System.out.println("Formals are: " + formals);
						f.pushBack(); 
						if(flag==0 && formals.compareTo("")!=0)
							System.out.println("ERROR : Invalid method param\n");
						return formals; 
					}
					if (current_op == ',' && flag==0) {
						System.out.println("ERROR : Invalid method param\n");
					}			
					if (current_op == ',' && flag==1) {
						flag = 0;
					}
					break;
				case WORD:
					f.match("int");
					if(f.peekAtKind()!=TokenType.WORD || flag!=0)
						System.out.println("ERROR : Invalid method param\n");
					flag = 1;
					formals += f.getWord();
					formals += " ";
					break;
				default:
					System.out.println("ERROR : Invalid formals\n");

			}


		}while (true);
	}


	public 	boolean IsBlockKeyWord(String in_string){
		List<String> messages = Arrays.asList("while", "return", "if");
		return messages.contains(in_string);
	}

	public 	boolean IsKeyChar(String in_string){
		List<String> messages = Arrays.asList("(", ")", "+", "-", ";", ",");
		return messages.contains(in_string);
	}

	public 	boolean IsTypeWord(String in_string){
		List<String> types = Arrays.asList("int");
		return types.contains(in_string);
	}

	//covers scenarios:
	//1. declaration w/o instantiation
	//2. declration w/o  instantiation
	String getDeclaration(SamTokenizer f, symbol_table method_symbol_table){
		String declaration= "";
		while(true){ 
			if(f.peekAtKind()!=TokenType.WORD){
				System.out.println("ERROR: variable names invalid\n");
				System.exit(1);
			}
			String variable_name = f.getWord();
			//System.out.print("variable_name: " + variable_name + "\n"); 
			if (IsBlockKeyWord(variable_name) || IsTypeWord(variable_name)) { //sanity check for the name of the vars
				System.out.println("ERROR: variable names can not be a keyword\n");
				System.exit(1);
			}else{
				if (method_symbol_table.has_var(variable_name) == false){
					//declaration += "PUSHIMM 0\n";
					method_symbol_table.add_locals(variable_name);
					method_symbol_table.add_variable(variable_name);
				}else{
					System.out.println("Already defined\n");
					System.exit(1);
				}
			}

			if(f.peekAtKind()==TokenType.OPERATOR){
				//if also instantiated 
				char next_char = f.getOp(); 
				if (next_char == '='){
					declaration += getExp(f, method_symbol_table); 
					f.match(';'); 
					int offSet = method_symbol_table.get_local_offset(variable_name);
					declaration += "STOREOFF " + offSet + "\n"; 
					//System.out.print("variable_def: " + variable_definition); 
					//declaration += variable_definition; 
					break; 
				} else if (next_char == ','){
					declaration += getDeclaration(f,method_symbol_table);
				}else if (next_char == ';'){
					break;
				}
				else{
					System.out.println("ERROR : Illegal operator in the declarations\n");
					System.exit(1);
				}
			}
		}
		return declaration; 
	}


	//instantiation 
	String getInstantiation(SamTokenizer f, symbol_table method_symbol_table){
		String declaration= "";
		String variable_name = f.getWord();
		if (IsBlockKeyWord(variable_name) || IsTypeWord(variable_name)) { //sanity check for the name of the vars
			System.out.println("ERROR: variable names can not be a keyword\n");
			System.exit(1);
		}
		if (IsKeyChar(variable_name)) {
			System.out.println("ERROR: variable names can not be a keyChar\n");
			System.exit(1);
		}

		//System.out.print("variable_name: " + variable_name + "\n"); 
		f.match('=');
		String variable_definition = getExp(f, method_symbol_table);

		int offSet = 0; 
		if (method_symbol_table.has_param(variable_name)) {
			offSet = method_symbol_table.get_param_offset(variable_name);
		}else if (method_symbol_table.has_local(variable_name)) {
			offSet = method_symbol_table.get_local_offset(variable_name);
		}else{
			System.out.println("this variable is not defined\n");
			System.exit(0);
		}
		variable_definition +=  "STOREOFF " + offSet + "\n"; 
		f.match(';'); 
		return variable_definition;
	}


	String returnLine(SamTokenizer f){ //delimeted by ';'
		String line = "";
		int counter = 0; 
		int done = 0; 
		while(true){
			switch(f.peekAtKind()){
				case INTEGER:
					{
						line += f.getInt();
						break;
					}
				case OPERATOR:
					{ 
						char current_op =  f.getOp();
						if (current_op == ';') {
							done = 1;
							break;  
						}
						f.pushBack(); 
						line += f.getOp();
						break; 
					}
				default:
					{
						line += f.getWord(); 
						break; 
					}
			} 
			counter +=1; 
			line+=" ";
			if (done == 1){
				break;
			}
		}
		while(counter > 0) {
			f.pushBack();
			counter -=1;
		}
		return line;
	}

	String getBlockKeyWordExp(SamTokenizer f, symbol_table method_symbol_table)  {
		String body = "";
		String current_word = f.getWord(); 

		if(current_word.equals("while")){ // is it a while
			body += getWhile(f, method_symbol_table);
		}else if (current_word.equals("if")){
			body += getIf(f, method_symbol_table);
		}else if (current_word.equals("return")){
			body += getReturn(f, method_symbol_table);
		}else if (current_word.equals("break")) {
			body += getBreak(f, method_symbol_table);
		}
		else{//error out other wise
			System.out.println("ERROR: not sure what to do with this: " + current_word + " as a keyword\n");
			System.exit(0);
		}
		return body;
	}


	String getStatement(SamTokenizer f, symbol_table method_symbol_table){
		String exp=""; 
		switch(f.peekAtKind()) {
			case OPERATOR:  
				{
					char next_char = f.getOp(); 
					if(next_char == '{') {
						while(true) {
							if(f.peekAtKind() == TokenType.OPERATOR) {
								if(f.getOp() == '}')
									break;
								else
									f.pushBack();	
							}
							exp+=getStatement(f, method_symbol_table);
						}
					}
					else if (next_char == ';')
						return exp;
					else {
						System.out.println("ERROR: Invalid operator in statement\n");
						System.exit(0);
					}
				}
			case WORD:  //either block key words or assignments
				{
					String theWord =  f.getWord();
					f.pushBack();
					if(IsBlockKeyWord(theWord)){ //this includes blocks and declarations
						return getBlockKeyWordExp(f, method_symbol_table);
					}else{ //variable assignment
						return  getInstantiation(f, method_symbol_table);
					}
				}
			default: 
				{
					System.out.printf("this statement can not be parsed\n");
					return "ERROR\n";
				}
		}
	}

	String getBody(SamTokenizer f, symbol_table method_symbol_table){
		String body = "";
		String current_word = f.getWord();
		f.pushBack();
		while(IsTypeWord(current_word)){  // is it a declaration
			f.getWord();
			body += getDeclaration(f, method_symbol_table);
			if(f.peekAtKind()==TokenType.WORD) {
				current_word = f.getWord();
				f.pushBack();
			}
			else
				break;


		}
		do 
		{
			System.out.println("line is: " + returnLine(f) + "\n"); 
			body += getStatement(f, method_symbol_table);  
		} while (f.check('}') == false);
		f.pushBack();
		return body;
	}


        public static void main(String[] args) {
            try {
		BaliCompiler myCompile = new BaliCompiler();
                myCompile.compiler(args[0]);
            } catch (Exception e) { System.err.println (e); }
        }
}
