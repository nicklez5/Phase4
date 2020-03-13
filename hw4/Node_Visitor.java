import java.util.*;
import cs132.vapor.ast.*;
import cs132.vapor.ast.VInstr.Visitor;
import cs132.vapor.ast.VVarRef.Register;
import cs132.vapor.ast.VVarRef.Local;
import cs132.vapor.ast.VMemRef.Global;
import cs132.vapor.ast.VMemRef.Stack;
public class Node_Visitor extends VInstr.VisitorPR< Integer ,String, RuntimeException>{
    public int t;
    public Set<String> access_set;
    public Node_Visitor(){
        t = 0;
        access_set = new HashSet<String>();
    }
    public void init_t(){
        t = 0;
    }
    public String visit(Integer p, VAssign a){
        //System.out.println("Integer: " + p);
        String _ret = "";
        String _dest = a.dest.toString();
        String _rhs = a.source.toString();

        if(a.source instanceof VLitStr){
            VLitStr temp_x = (VLitStr)a.source;
            //System.out.println("VAssign - String Value: " + temp_x.toString());

        }else if(a.source instanceof VLitInt){
            VOperand.Static list_args = (VOperand.Static)a.source;
            VLitInt int_literal = (VLitInt)list_args;
            System.out.println("  " + "li " + _dest + " " + int_literal.toString());
        }else if(a.source instanceof Register){
            System.out.println("  " + "move " + _dest + " " + _rhs);
        }else if(a.source instanceof VLabelRef){
            VLabelRef temp_label = (VLabelRef)a.source;
            System.out.println("  " + "la " + _dest + " " + temp_label.ident);
            //System.out.println("Label Ref: " + temp_label.ident);
        }
        if(a.dest instanceof VVarRef.Local){
            VVarRef.Local temp_local = (VVarRef.Local)a.dest;
            //System.out.println("  " + "local was here");
            //System.out.println("VAssign - Store to: " + temp_local.toString());

        }
        return _ret;
    }
    /*
    VAddr<VFunction> addr; address of function being called
    VOperand[] args - arguments pass into the functions
    VVarRef.Local dest - variable used to store the return value of the function - null also
    */
    public String visit(Integer p , VCall c){
        String _ret = "";
        if(c.addr instanceof VAddr.Label){
            VAddr.Label temp_label = (VAddr.Label)c.addr;
            _ret = temp_label.toString();
            _ret = _ret.replace(":","");
            System.out.println("  jal " + _ret);
            //System.out.println("VCall Addr Function Label: " + temp_label.toString());
        }else if(c.addr instanceof VAddr.Var){
            VAddr.Var temp_var = (VAddr.Var)c.addr;
            //System.out.println("VCall Addr Function Variable: " + temp_var.toString());
            System.out.println("  " + "jalr " + temp_var.toString());
        }
        if(c.dest != null){
            //System.out.println("VCall Dest: " + c.dest.toString());

        }
        VOperand[] list_args = c.args;
        for(int i = 0 ; i < list_args.length ; i++){
            if(list_args[i] instanceof VLitStr){
                //System.out.println("VCall - String Literal Argument " + i + ": " + list_args[i].toString());

            }else if(list_args[i] instanceof VVarRef){
                VVarRef temp_var_ref = (VVarRef)list_args[i];
                if(temp_var_ref instanceof VVarRef.Local){
                    VVarRef.Local temp_var_ref_local = (VVarRef.Local)temp_var_ref;
                    //System.out.println("VCall - Local Argument: " + temp_var_ref.toString());

                }

            }else if(list_args[i] instanceof VLitInt){
                VOperand.Static temp_static = (VOperand.Static)list_args[i];
                VLitInt literal_int = (VLitInt)temp_static;
                //System.out.println("VCall - Integer Literal Argument " + i + ": " + literal_int.toString());

            }else if(list_args[i] instanceof VLabelRef){
                VLabelRef holy_label = (VLabelRef)list_args[i];
                System.out.println("  jal " + holy_label.ident);
            }
        }

        return _ret;
    }
    public String return_cmp_label(String built_in_label){
        String _ret = "";
        if(built_in_label.contains("LtS")){
            _ret = "slti";
        }else if(built_in_label.contains("Sub")){
            _ret = "subu";
        }else if(built_in_label.contains("Add")){
            _ret = "addu";
        }else if(built_in_label.contains("MulS")){
            _ret = "mul";
        }
        return _ret;
    }
    /*
    Op op - operation being performed
    VOperand[] args- arguments to the operations
    VVarRef dest - variable/register to store the result of the operation
    */
    public String visit(Integer p ,VBuiltIn c) {
        String _ret = "";
        boolean cmp_value = false;
        String built_in_label = c.op.name;
        String cmp_string = "";
        if(built_in_label.contains("LtS") || built_in_label.contains("Sub") || built_in_label.contains("Add") || built_in_label.contains("MulS")){
            cmp_string = return_cmp_label(built_in_label);
            cmp_value = true;
        }
        //System.out.println("Index: " + Integer.toString(p));
        //System.out.println("Op Name: " + c.op.name + " Param Size: " + c.op.numParams);
        //System.out.println("Destination: " + c.dest);
        if(c.dest instanceof VVarRef.Local){
            VVarRef.Local temp_local = (VVarRef.Local)c.dest;
            //System.out.println("Dest Local: " + temp_local.toString());

        }

        VOperand[] list_args = c.args;
        for(int i = 0 ; i < list_args.length ; i++){
            if(cmp_value){
                System.out.println("  " + cmp_string + " " + c.dest + " " + list_args[i].toString() + " " + list_args[i+1].toString());
                break;
            }
            if(list_args[i] instanceof VLitStr){
                //System.out.println("Index: " + Integer.toString(p)  + " VBuilt in - String Literal Argument " + i + ": " + list_args[i].toString());
                if(list_args[i].toString().contains("null")){
                    System.out.println("  " + "la $a" + i + " _str0");
                }
            }else if(list_args[i] instanceof VVarRef){
                VVarRef temp_var_ref = (VVarRef)list_args[i];
                if(temp_var_ref instanceof VVarRef.Local){
                    VVarRef.Local temp_var_ref_local = (VVarRef.Local)temp_var_ref;
                    //System.out.println("VBuiltIn - Local Argument: " + temp_var_ref_local.toString());
                    //in_set.add(integer_literal.toString());
                }else if(temp_var_ref instanceof VVarRef.Register){
                    VVarRef.Register temp_var_ref_reg = (VVarRef.Register)temp_var_ref;
                    System.out.println("  " + "move " + "$a" + i + " " + temp_var_ref_reg.toString());
                }
            }else if(list_args[i] instanceof VLitInt){
                VOperand.Static static_value = (VOperand.Static)list_args[i];
                VLitInt integer_literal = (VLitInt)static_value;
                System.out.println("  " + "li $a" + i + " " + integer_literal.toString());
                //System.out.println("VBuiltIn - Integer Literal: " + integer_literal.toString());
                //in_set.add(integer_literal.toString());
            }else if(list_args[i] instanceof Register){

            }
        }
        return_built_in(built_in_label);
        return _ret;
    }
    public void return_built_in(String op_name){
        String _ret = "";
        if(op_name.equals("HeapAllocZ")){
            _ret = op_name.replace("H","h");
            _ret = _ret.replace("Z","");
            _ret = "_" + _ret;
            System.out.println("  " + "jal " + _ret);
            access_set.add("heap");
        }else if(op_name.equals("Error")){
            System.out.println("  " + "j _error");
            access_set.add("error");
        }else if(op_name.equals("PrintIntS")){
            System.out.println("  " + "jal _print");
            access_set.add("print");
        }
    }
    /*
    VMemRef dest - memory location being written to
    VOperand source- value being written.
    */
    public String visit(Integer p , VMemWrite w){
        String _ret = "";


        if(w.dest instanceof VMemRef.Global){

            //VMemRef data = w.dest;
            VMemRef.Global c2 = (VMemRef.Global)w.dest;

            VAddr<VDataSegment> holy_one = c2.base;
            if(holy_one instanceof VAddr.Label){
                VAddr.Label temp_label = (VAddr.Label)holy_one;
                //System.out.println("Write to Addr of Label: " + temp_label.toString());
            }else if(holy_one instanceof VAddr.Var){
                VAddr.Var temp_var = (VAddr.Var)holy_one;
                //System.out.println("Write to Addr of Variable: " + temp_var.toString());
                System.out.println("  " + "move " + temp_var.toString() + " $v0");

            }
            VOperand list_args = w.source;
            VOperand.Static temp_label = (VOperand.Static)list_args;

            if(list_args instanceof VLitStr){
                //System.out.println("VMemWrite - String Literal Argument: " + list_args.toString());
            }else if(list_args instanceof VVarRef){
                VVarRef temp_var_ref = (VVarRef)list_args;
                if(temp_var_ref instanceof VVarRef.Local){
                    VVarRef.Local temp_var_ref_local = (VVarRef.Local)temp_var_ref;
                    //System.out.println("VMemWrite - Local Argument: " + temp_var_ref_local.toString());

                }
            }else if(temp_label instanceof VLitInt){
                VOperand.Static list_value = (VOperand.Static)list_args;
                VLitInt integer_literal = (VLitInt)list_value;
                //System.out.println("VMemWrite - Integer Literal: " + integer_literal.toString());

            }else if(temp_label instanceof VLabelRef){
                VLabelRef temp_label_ref = (VLabelRef)temp_label;
                System.out.println("  " + "la $t9 " + temp_label_ref.ident);
            }
        }
        return _ret;

        //System.out.println("VMemWrite was accessed");
    }
    /*
    VVarRef dest - variable/register to store the value isnt_found
    VMemRef source - memory location being read
    */
    public String visit(Integer p ,VMemRead r)  {
        String _ret = "";
        String _dest = r.dest.toString();
        if(r.source instanceof VMemRef.Global){
            VMemRef.Global _global = (VMemRef.Global)r.source;
            //VMemRef.Global c = r.source;
            VAddr<VDataSegment> c2 = _global.base;
            if(c2 instanceof VAddr.Label){
                VAddr.Label temp_label = (VAddr.Label)c2;
                //System.out.println("Read Addr of Label: " + temp_label.toString());
            }else if(c2 instanceof VAddr.Var){
                VAddr.Var temp_var = (VAddr.Var)c2;
                System.out.println("  " + "lw " + _dest + " 0(" + temp_var.toString() + ")");

            }
        }
        if(r.dest instanceof VVarRef.Local){
            VVarRef.Local temp_local = (VVarRef.Local)r.dest;
            //System.out.println("Index: " + Integer.toString(p) + " Store to: " + temp_local.toString());

        }
        return _ret;
        //System.out.println("VMemRead was accessed");
    }
    /*
    VOperand value - value
    */
    public String visit(Integer p ,VBranch b) {
        String _ret = "";
        String branch_target = b.target.toString();
        branch_target = branch_target.replace(":","");
        //System.out.println("Current_index: " + Integer.toString(p) + " Goto branch " + b.target.toString());
        //System.out.println("Branch Boolean Value: " + b.positive);
        if(b.positive){
            System.out.println("  " + "sw $t9 0($t0)");
            System.out.println("  " + "bnez $t0 " + branch_target);
        }else{
            System.out.println("  " + "beqz " + b.value + " " + branch_target);
        }
        _ret = b.target.toString();


        //System.out.println("VBranch was accessed");
        VOperand list_args = b.value;
        if(list_args instanceof VOperand.Static){
            VOperand.Static temp_branch_label = (VOperand.Static)list_args;
            VLabelRef da_label = (VLabelRef)temp_branch_label;
            //System.out.println("Branch Label: " + da_label.ident);
        }
        if(list_args instanceof VLitStr){
            //System.out.println("VBranch - String Literal Argument: " + list_args.toString());
        }else if(list_args instanceof VVarRef){
            VVarRef temp_var_ref = (VVarRef)list_args;
            if(temp_var_ref instanceof VVarRef.Local){
                //System.out.println("VBranch - Local Argument: " + temp_var_ref.toString());

            }
        }else if(list_args instanceof VLitInt){
            VOperand.Static list_value = (VOperand.Static)list_args;
            VLitInt integer_literal = (VLitInt)list_value;
            //System.out.println("VBranch - Integer Literal: " + integer_literal.toString());
        }
        return _ret;
    }
    public String visit(Integer p, VGoto g)  {
        String _ret = "";
        VAddr<VCodeLabel> temp_g = g.target;
        if(temp_g instanceof VAddr.Label){
            VAddr.Label temp_g2 = (VAddr.Label)temp_g;
            String str_value = temp_g2.toString();
            str_value = str_value.replace(":","");
            System.out.println("  " + "j " + str_value);
            //System.out.println("Current_index: " + Integer.toString(p) + " Goto " + temp_g2.toString());
            _ret = temp_g2.toString();


        }else if(temp_g instanceof VAddr.Var){
            VAddr.Var temp_g3 = (VAddr.Var)temp_g;
            //System.out.println("Current_index: " + Integer.toString(p) + " Goto " + temp_g3.toString());
            _ret = temp_g3.toString();

        }

        return _ret;
        //System.out.println("VGoto was accessed");
    }
    public String visit(Integer p , VReturn r)  {
        String _ret = "";
        VOperand list_args = r.value;
        if(list_args instanceof VOperand.Static){
            VOperand.Static return_label = (VOperand.Static)list_args;
            VLabelRef da_label = (VLabelRef)return_label;
            //System.out.println("Return Label: " + da_label.ident);
        }
        if(list_args instanceof VLitStr){
            //System.out.println("VReturn - String Literal Argument: " + list_args.toString());
        }else if(list_args instanceof VVarRef.Local){
            //System.out.println("VReturn - Local Argument: " + list_args.toString());

        }else if(list_args instanceof VLitInt){
            VOperand.Static list_value = (VOperand.Static)list_args;
            VLitInt integer_literal = (VLitInt)list_value;
            //System.out.println("VReturn - Integer Literal: " + integer_literal.toString());
        }
        //System.out.println("VReturn was accessed");
        return _ret;
    }
}
