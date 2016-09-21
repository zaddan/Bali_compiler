package Bali_package;
public class function_info{
    String code;
    int n_args;
    public function_info(int n_args, String code){
        this.code = code;
        this.n_args = n_args;
    }
    
    public String get_code(){
        return code;
    }

    public int get_n_args(){
        return n_args;
    }
}
