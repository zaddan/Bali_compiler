package Bali_package;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Hashtable;
 
public class symbol_table{
    ArrayList variable_list = new ArrayList();
    ArrayList params_list = new ArrayList();
    ArrayList locals_list = new ArrayList();
    
    public void add_variable(String variable){
        variable_list.add(variable);
    }
 
    
    public void add_locals(String variable){
        locals_list.add(variable);
    }
    
   
    public void add_params(String variable){
        params_list.add(variable);
    }
    

    public boolean has_var(String variable){
        return variable_list.contains(variable); 
    }
    
    public boolean has_param(String variable){
        return params_list.contains(variable); 
    }
    
    public boolean has_local(String variable){
        return locals_list.contains(variable); 
    }

    public int get_n_locals(){
        return locals_list.size(); 
    }
 
    public int get_n_params(){
        return params_list.size(); 
    }   


    
    public int get_param_offset(String variable){
        if (params_list.contains(variable)) {
            return params_list.indexOf(variable);
        }else{
            System.out.println("ERROR. this variable is not defined in the symbol table\n");
            print_symbol_table_content();
            System.exit(0); 
            return 0;
        }
    }
    
    
    public int get_local_offset(String variable){
        if (locals_list.contains(variable)) {
            return locals_list.indexOf(variable);
        }else{
            System.out.println("ERROR. this variable is not defined in the symbol table\n");
            print_symbol_table_content();
            System.exit(0); 
            return 0;
        }
    }
    

    public void print_symbol_table_content(){
        System.out.println("******here is the list of the variables and their offset\n");
        for (int i =0; i < variable_list.size(); i++){
            System.out.println("index " + i + ": " + variable_list.get(i) + "\n");
        }       

        for (int i =0; i < params_list.size(); i++){
            System.out.println("index " + i + ": " + params_list.get(i) + "\n");
        }
        for (int i =0; i < locals_list.size(); i++){
            System.out.println("index " + i + ": " + locals_list.get(i) + "\n");
        }  
        System.out.println("****here is the list of functions\n");
    }
}
