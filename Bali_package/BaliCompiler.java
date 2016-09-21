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
			String pgm="";
		    //main_preparation();	
            while(f.peekAtKind()!=TokenType.EOF) // till EOF, find another method
			{
                getMethod(f);
            }
		    	

            return main_preparation() + prg_func_table.get_function_code("main");
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
		String methodName = f.getWord(); 
		System.out.println(methodName);
		
        function_end_label_ll.add(methodName);
        label_counter +=1;

		//method formals
        f.match('(');
		String formals = getFormals(f);	     	
        f.match(')');  // must be an closing parenthesis
        System.out.println("formals: " + formals); 
        //System.out.println("End Of Formals"); 
        
        //fill up the symbol_table with the formals
        String[] formals_splited= formals.split(" ");
        for (int i=0; i< formals_splited.length; i++){
            method_symbol_table.add_variable(formals_splited[i]);
            method_symbol_table.add_params(formals_splited[i]);
        }
        
        //callee prepartion 
        //function_code += callee_preparation(formals);

	    //body 
        f.match('{');
		String body = getBody(f, method_symbol_table);
		f.match('}');  // must be an closing parenthesis
        System.out.println("body:\n" + body + "\n"); 
        //System.out.println("End Of body"); 
        
        
        function_code = "ADDSP " + method_symbol_table.get_n_locals() + "\n";
        function_code += body;
        String end_label = "fEnd"+label_counter;
        function_code += end_label +":";
        function_code += "TODO: STOREOFF rv slot" + "\n"; 
        function_code += "JUMPIND\n";
        //update function_table

        prg_func_table.add_function(methodName, formals_splited.length, function_code );
        return null;
	}
	
    String getWhile(SamTokenizer f, symbol_table method_symbol_table){
        f.match('(');
        String while_predicate = getExp(f, method_symbol_table);
        f.match(')');
        f.match('{'); 
        String while_body = getBody(f, method_symbol_table);  
        f.match('}'); 
        
        
        String while_begin_label =  "Label" + label_counter;
        label_counter +=1;
       

        String predicate_true_label =  "Label" + label_counter;
        label_counter +=1;

        String while_code = "JUMP " + while_begin_label + "\n";
        while_code += predicate_true_label + ":" + while_body;
        while_code += while_begin_label + ":" + while_predicate; 
        while_code += "JUMPC " +  predicate_true_label + "\n";
        return while_code;
    }
    
    String getIf(SamTokenizer f, symbol_table method_symbol_table){
        f.match('(');
        String if_predicate = getExp(f, method_symbol_table);
        f.match(')');
        String false_label =  "Label" + label_counter;
        label_counter +=1;
        String if_false = "JUMPC " + false_label + "\n";
        
        String if_body = getStatement(f, method_symbol_table);  
         
        String if_code = if_predicate;
        if_code += if_false;
        if_code += if_body;
        return if_code;
    }
    
   String getReturn(SamTokenizer f, symbol_table method_symbol_table){
        String exp = getExp(f, method_symbol_table);  
        f.match(';');
        
        String return_label = "fEnd"+(String) function_end_label_ll.getLast();
        function_end_label_ll.removeLast();
        
        exp += "JUMP " + return_label + ":" + "\n"; 
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
                switch(f.getOp()){
                    case ('('):
                    {
                        String someExp = getExp(f, method_symbol_table); 
                        char nextOp =  f.getOp();
                        if (nextOp == '+') {
                            exp += someExp;
                            exp += getExp(f, method_symbol_table);
                            exp += "ADD\n";
                            f.match(')'); 
                        }else if (nextOp == '-' ||  nextOp == '>' || nextOp == '<') {
                            exp += someExp;
                            exp += getExp(f, method_symbol_table);
                            exp += "SUB\n";
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
                                    return caller_preparation() + prg_func_table.get_function_code(theWord); 
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
                    return  "PUSHOFF " + offSet +";\n";
                }
            }
            default:   return "ERROR\n";
        }
	}
	
    
    String getFormals(SamTokenizer f){
		String formals = "";
        do 
		{
            switch(f.peekAtKind()){
                case OPERATOR:
                { 
                    char current_op =  f.getOp();
                    if (current_op == ')') {
                        //System.out.println("Formals are: " + formals);
                        f.pushBack(); 
                        return formals; 
                    }
                }
            }

            f.match("int");
		    formals += f.getWord();
		    formals += " ";
		
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
         String current_word = f.getWord();
         if (current_word.equals("int")){ 
             while(true){ 
                 String variable_name = f.getWord();
                 //System.out.print("variable_name: " + variable_name + "\n"); 
                 if (IsBlockKeyWord(variable_name) || IsTypeWord(variable_name)) { //sanity check for the name of the vars
                     System.out.println("ERROR: variable names can not be a keyword\n");
                     System.exit(1);
                 }else{
                     if (method_symbol_table.has_var(variable_name) == false){
                        declaration += "PUSHIMM 0\n";
                        method_symbol_table.add_locals(variable_name);
                        method_symbol_table.add_variable(variable_name);
                     }
                 }
                 
                 //if also instantiated 
                 char next_char = f.getOp(); 
                 if (next_char == '=' ||  next_char == ','){
                     String variable_definition = getExp(f, method_symbol_table);
                     f.match(';'); 
                     declaration += variable_definition; 
                     int offSet = 0; 
                     if (method_symbol_table.has_param(variable_name)) {
                         offSet = method_symbol_table.get_param_offset(variable_name);
                     }else if (method_symbol_table.has_local(variable_name)) {
                         offSet = method_symbol_table.get_local_offset(variable_name);
                     }else{
                         System.out.println("this variable is not defined\n");
                         System.exit(0);
                     }
                     declaration += "STOREOFF " + offSet + "\n"; 
                     //System.out.print("variable_def: " + variable_definition); 
                     //declaration += variable_definition; 
                     break; 
                 }else if (next_char == ';'){
                     break;
                 }
             }
         }else{
             System.out.println(" declaration can not start with anything but an int\n");
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
            //f.pushBack(); 
            body += getWhile(f, method_symbol_table);
        }else if (current_word.equals("if")){
            body += getIf(f, method_symbol_table);
        }else if (current_word.equals("return")){
            body += getReturn(f, method_symbol_table);
        }
        else{//error out other wise
            System.out.println("ERROR: not sure what to do with this: " + current_word + " as a keyword\n");
            System.exit(0);
        }
        return body;
    }
    
    
    String getStatement(SamTokenizer f, symbol_table method_symbol_table){
        //String current_token =  f.peekAtKind(); //take a peek the word
        String exp; 
        switch(f.peekAtKind()) {
            case INTEGER: //do nothing
            {
               return "PUSHIMM " +f.getInt() + "\n"; //convert to string
             }
            case OPERATOR:  //evaluate the expression
            {
                /* 
                char theOp =  f.getOp();
                f.pushBack();
                if (theOp == '}'){
                    return "}"; 
                }
                */ 
                exp = getExp(f, method_symbol_table); 
                f.match(';');
                return exp;
            }
            case WORD:  //either blocks, declarations, assignements, function calls
            {
                String theWord =  f.getWord();
                f.pushBack();
                
                if(IsTypeWord(theWord)){  // is it a declaration
                    String declaration = getDeclaration(f, method_symbol_table);
                    return declaration;
                }
                if(IsBlockKeyWord(theWord)){ //this includes blocks and declarations
                    return getBlockKeyWordExp(f, method_symbol_table);
                }
                else if (prg_func_table.has_func(theWord)){ //it's a function call
			        exp = getExp(f, method_symbol_table); 
                    f.match(';');
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
        
        do 
        {
            System.out.println("line is: " + returnLine(f) + "\n"); 
            body += getStatement(f, method_symbol_table);  
        }while (f.check('}') == false);
		f.pushBack();
        return body;
	}
}
