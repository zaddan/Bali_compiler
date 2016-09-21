package Bali_package;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Hashtable;
import Bali_package.function_info ;



public class function_table{
    
    Hashtable<String, function_info> function_code_table = new Hashtable<String, function_info>();    

    public void add_function(String function_name, int n_args, String code){
        function_info my_foo =  new function_info(n_args, code);
        function_code_table.put(function_name, my_foo);
        System.out.println(get_function_code(function_name));
    }
   

    public String get_function_code(String function_name){
        if(function_code_table.containsKey(function_name) == false) {
            System.out.println("function: " + function_name + " has not been added to the function_code_table");
            System.exit(0); 
        }
        return function_code_table.get(function_name).get_code();
    }

   
     public int get_function_n_args(String function_name){
        if(function_code_table.containsKey(function_name) == false) {
            System.out.println("function: " + function_name + " has not been added to the function_code_table");
            System.exit(0); 
        }
        return function_code_table.get(function_name).get_n_args();
    }

    

    public boolean has_func(String function_name){
        return (function_code_table.containsKey(function_name)); 
    }
    
    public void print_symbol_table_content(){
       System.out.println("****here is the list of functions\n");
       for (String key : function_code_table.keySet()) {
           System.out.println(key);
       }
    }

}
