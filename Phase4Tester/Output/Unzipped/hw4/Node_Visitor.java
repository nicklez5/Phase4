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
    public int local_stack_index;
    public int local_in_index;
    public int local_out_index;
    public boolean local_loop;
    public Map<Integer,List<String>> node_label_map;
    public Node_Visitor(){
        t = 0;
        access_set = new HashSet<String>();
        node_label_map = new HashMap<Integer,List<String>>();
        local_stack_index = 0;
        local_loop = false;
        local_in_index = 0;
        local_out_index = 0;
    }
    public void set_local_label_map(Map<Integer,List<String>> temp_map){
        node_label_map = temp_map;
    }
    public void clear_index(){
        local_stack_index = 0;
        local_in_index = 0;
        local_out_index = 0;
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
            System.out.println("VAssign - String Value: " + temp_x.toString());

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
            _ret = "slt";
        }else if(built_in_label.contains("Sub")){
            _ret = "subu";
        }else if(built_in_label.contains("Add")){
            _ret = "addu";
        }else if(built_in_label.contains("MulS")){
            _ret = "mul";
        }else if(built_in_label.contains("Lt")){
            _ret = "sltu";
        }
        return _ret;
    }
    public String check_args(VOperand[] lists_argz, String cmp_stringz){
        String _ret = cmp_stringz;
        boolean number_true = false;
        String _value = "";
        boolean _lhs_number = false;

        for(int i = 0;i < lists_argz.length ;i++){
            if(lists_argz[i] instanceof VLitInt){
                VLitInt lit_interal = (VLitInt)lists_argz[i];
                number_true = true;
                _value = lit_interal.toString();
                break;
            }
            if(lists_argz[0] instanceof VLitInt){
                _lhs_number = true;
            }

        }
        if(number_true){
            if(cmp_stringz.equals("slt")){
                _ret = "slti";
            }else if(cmp_stringz.equals("subu")){
                if(_lhs_number){
                    System.out.println("  li $t9 " + _value);
                }
            }

        }


        return _ret;
    }
    public boolean check_if_number(VOperand[] list_args){
        for(int i = 0 ; i < list_args.length ; i++){
            if(list_args[i] instanceof VLitInt){
                return true;
            }
        }
        return false;
    }
    /*
    Op op - operation being performed
    VOperand[] args- arguments to the operations
    VVarRef dest - variable/register to store the result of the operation
    */
    public String visit(Integer p ,VBuiltIn c) {
        String _ret = "";
        boolean cmp_value = false;
        boolean int_value = false;
        String built_in_label = c.op.name;
        String dest_value = "";
        String cmp_string = "";
        if(built_in_label.contains("LtS") || built_in_label.contains("Sub") || built_in_label.contains("Add") || built_in_label.contains("MulS") || built_in_label.contains("Lt")){
            cmp_string = return_cmp_label(built_in_label);
            cmp_value = true;
        }
        //System.out.println("Index: " + Integer.toString(p));
        //System.out.println("Op Name: " + c.op.name + " Param Size: " + c.op.numParams);
        //System.out.println("Destination: " + c.dest);
        if(c.dest instanceof VVarRef.Local){
            VVarRef.Local temp_local = (VVarRef.Local)c.dest;
            dest_value = temp_local.toString();
            //System.out.println("Dest Local: " + temp_local.toString());
        }else if(c.dest instanceof VVarRef.Register){
            VVarRef.Register temp_reg = (VVarRef.Register)c.dest;
            dest_value = temp_reg.toString();
        }

        VOperand[] list_args = c.args;
        cmp_string = check_args(list_args,cmp_string);
        int_value = check_if_number(list_args);
        for(int i = 0 ; i < list_args.length ; i++){
            if(cmp_value){
                String _str_lhs = list_args[i].toString();
                String _str_rhs = list_args[i+1].toString();
                if(cmp_string.equals("subu") && int_value){
                    if(_str_lhs.matches("-?\\d+(\\.\\d+)?") && !_str_rhs.matches("-?\\d+(\\.\\d+)?")){
                        System.out.println("  li $t9 " + _str_lhs);
                        System.out.println("  " + cmp_string + " " + c.dest + " $t9 " + _str_rhs);
                        break;
                    }else if(_str_lhs.matches("-?\\d+(\\.\\d+)?") && _str_rhs.matches("-?\\d+(\\.\\d+)?")){
                        int number_1 = Integer.parseInt(_str_lhs);
                        int number_2 = Integer.parseInt(_str_rhs);
                        int total_value = number_1 - number_2;
                        System.out.println("  li " + c.dest + " " + total_value);
                        break;
                    }
                }else if(cmp_string.equals("sltu") && int_value){
                    if(_str_lhs.matches("-?\\d+(\\.\\d+)?") && !_str_rhs.matches("-?\\d+(\\.\\d+)?")){
                        System.out.println("  li $t9 " + _str_lhs);
                        System.out.println("  " + cmp_string + " " + c.dest + " $t9 " + _str_rhs);
                        break;
                    }
                }else if(cmp_string.equals("mul") && int_value){
                    if(_str_lhs.matches("-?\\d+(\\.\\d+)?") && _str_rhs.matches("-?\\d+(\\.\\d+)?")){
                        int number_1 = Integer.parseInt(_str_lhs);
                        int number_2 = Integer.parseInt(_str_rhs);
                        int total_value = number_1 * number_2;
                        System.out.println("  li " + c.dest + " " + total_value);
                        break;
                    }else if(_str_lhs.matches("-?\\d+(\\.\\d+)?") && !_str_rhs.matches("-?\\d+(\\.\\d+)?")){
                        System.out.println("  " + cmp_string + " " + c.dest + " " + _str_rhs + " " + _str_lhs);
                        break;
                    }
                }
                System.out.println("  " + cmp_string + " " + c.dest + " " + list_args[i].toString() + " " + list_args[i+1].toString());
                break;
            }
            if(list_args[i] instanceof VLitStr){
                //System.out.println("Index: " + Integer.toString(p)  + " VBuilt in - String Literal Argument " + i + ": " + list_args[i].toString());
                if(list_args[i].toString().contains("null")){
                    System.out.println("  " + "la $a" + i + " _str0");
                }else if(list_args[i].toString().contains("array index")){
                    System.out.println("  " + "la $a" + i + " _str1");
                    access_set.add("array");
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
        return_built_in(built_in_label, dest_value);

        return _ret;
    }
    public void return_built_in(String op_name, String dest_name){
        String _ret = "";
        if(op_name.equals("HeapAllocZ")){
            _ret = op_name.replace("H","h");
            _ret = _ret.replace("Z","");
            _ret = "_" + _ret;
            System.out.println("  " + "jal " + _ret);
            System.out.println("  " + "move " + dest_name + " $v0");
            /*
            if(dest_name.contains("$s")){
                System.out.println("  " + "move " + dest_name + " $v0");
            }
            */
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
        String _RHS = w.source.toString();
        String _LHS = w.dest.toString();
        if(w.dest instanceof VMemRef.Global){
            local_loop = true;
            clear_index();
            VOperand list_args = w.source;
            //VMemRef data = w.dest;
            VMemRef.Global c2 = (VMemRef.Global)w.dest;

            VAddr<VDataSegment> holy_one = c2.base;
            if(holy_one instanceof VAddr.Label){
                VAddr.Label temp_label = (VAddr.Label)holy_one;
                //System.out.println("Write to Addr of Label: " + temp_label.toString());
            }else if(holy_one instanceof VAddr.Var){
                VAddr.Var temp_var = (VAddr.Var)holy_one;
                //System.out.println("Base Offset: " + c2.byteOffset);
                if(c2.byteOffset == 0){
                    if(list_args instanceof VVarRef.Register){
                        VVarRef.Register holy_reg = (VVarRef.Register)list_args;
                        System.out.println("  " + "sw $" + holy_reg.ident + " 0(" + temp_var.toString() + ")");
                    }else if(list_args instanceof VLabelRef){
                        //System.out.println("  " + "move " + temp_var.toString() + " $v0");
                    }
                }else{

                    if(_RHS.matches("-?\\d+(\\.\\d+)?")){
                        System.out.println("  li $t9 " + _RHS);
                        System.out.println("  sw $t9 " + c2.byteOffset + "(" + temp_var.toString() + ")");
                    }else{
                        if(!_RHS.contains("$")) {
                            _RHS = "$" + _RHS;
                        }
                        System.out.println("  " + "sw " + _RHS + " " + c2.byteOffset + "(" + temp_var.toString() + ")");
                    }
                }
            }

            if(list_args instanceof VLitStr){
                //System.out.println("VMemWrite - String Literal Argument: " + list_args.toString());
            }else if(list_args instanceof VVarRef){
                VVarRef temp_var_ref = (VVarRef)list_args;
                if(temp_var_ref instanceof VVarRef.Local){
                    VVarRef.Local temp_var_ref_local = (VVarRef.Local)temp_var_ref;
                    System.out.println("VMemWrite - Local Argument: " + temp_var_ref_local.toString());

                }
            }else if(list_args instanceof VLitInt){
                VOperand.Static list_value = (VOperand.Static)list_args;
                VLitInt integer_literal = (VLitInt)list_value;
                //System.out.println("VMemWrite - Integer Literal: " + integer_literal.toString());

            }else if(list_args instanceof VLabelRef){
                VLabelRef temp_label_ref = (VLabelRef)list_args;
                System.out.println("  " + "la $t9 " + temp_label_ref.ident);
                if(w.dest instanceof VMemRef.Global){
                    VMemRef.Global temp_global = (VMemRef.Global)w.dest;
                    VAddr<VDataSegment> temp_global_addr = temp_global.base;
                    _LHS = temp_global_addr.toString();
                    System.out.println("  " + "sw $t9 0(" + _LHS + ")");
                }else{
                    System.out.println("  " + "sw $t9 0($t0)");
                }

            }
        }else if(w.dest instanceof VMemRef.Stack){
            VMemRef.Stack stack_local = (VMemRef.Stack)w.dest;
            int indexof_stack = stack_local.index;
            indexof_stack = 4 * indexof_stack;
            if(_RHS.contains("$v")){
                if(stack_local.region == VMemRef.Stack.Region.Local){
                    System.out.println("  sw " + _RHS + " " + indexof_stack + "($sp)");
                }
            }else{
                if(!local_loop){
                    if(stack_local.region == VMemRef.Stack.Region.In){
                        System.out.println("  sw $s" + local_in_index + " " + indexof_stack + "($sp)");
                        local_in_index++;
                    }else if(stack_local.region == VMemRef.Stack.Region.Out){
                        System.out.println("  sw $s" + local_out_index + " " + indexof_stack + "($sp)");
                        local_out_index++;
                    }else if(stack_local.region == VMemRef.Stack.Region.Local){
                        System.out.println("  sw $s" + local_stack_index + " " + indexof_stack + "($sp)");
                        local_stack_index++;
                    }
                }else{
                    if(stack_local.region == VMemRef.Stack.Region.In){
                        System.out.println("  lw $s" + local_in_index + " " + indexof_stack + "($sp)");
                        local_in_index++;
                    }else if(stack_local.region == VMemRef.Stack.Region.Out){
                        System.out.println("  lw $s" + local_out_index + " " + indexof_stack + "($sp)");
                        local_out_index++;
                    }else if(stack_local.region == VMemRef.Stack.Region.Local){
                        System.out.println("  lw $s" + local_stack_index + " " + indexof_stack + "($sp)");
                        local_stack_index++;
                    }
                }
            }

        }
        return _ret;

        //System.out.println("VMemWrite was accessed");
    }
    public boolean check_if_label_contain(String label_id){

        for(Map.Entry<Integer,List<String>> entry: node_label_map.entrySet()){
            List<String> label_name_list = entry.getValue();
            if(label_name_list.contains(label_id)){
                return true;
            }
        }
        return false;
    }
    /*
    VVarRef dest - variable/register to store the value isnt_found
    VMemRef source - memory location being read
    */
    public String visit(Integer p ,VMemRead r)  {
        String _ret = "";
        String _dest = r.dest.toString();
        String _RHS = r.source.toString();
        if(r.source instanceof VMemRef.Global){
            local_loop = true;
            VMemRef.Global _global = (VMemRef.Global)r.source;
            //VMemRef.Global c = r.source;
            VAddr<VDataSegment> c2 = _global.base;
            if(c2 instanceof VAddr.Label){
                VAddr.Label temp_label = (VAddr.Label)c2;
                //System.out.println("Read Addr of Label: " + temp_label.toString());
            }else if(c2 instanceof VAddr.Var){
                VAddr.Var temp_var = (VAddr.Var)c2;
                System.out.println("  " + "lw " + _dest + " " + _global.byteOffset + "(" + temp_var.toString() + ")");

            }
        }else if(r.source instanceof VMemRef.Stack){
            VMemRef.Stack stack_local = (VMemRef.Stack)r.source;
            int indexof_stack = stack_local.index;
            indexof_stack = 4 * indexof_stack;
            if(_dest.contains("$v")){
                if(stack_local.region == VMemRef.Stack.Region.Local){
                    System.out.println("  lw $v0 " + indexof_stack + "($sp)");

                }
            }else{
                if(!local_loop){
                    if(stack_local.region == VMemRef.Stack.Region.In){
                        System.out.println("  sw $s" + local_in_index + " " + indexof_stack + "($sp)");
                        local_in_index++;
                    }else if(stack_local.region == VMemRef.Stack.Region.Out){
                        System.out.println("  sw $s" + local_out_index + " " + indexof_stack + "($sp)");
                        local_out_index++;
                    }else if(stack_local.region == VMemRef.Stack.Region.Local){
                        System.out.println("  sw $s" + local_stack_index + " " + indexof_stack + "($sp)");
                        local_stack_index++;
                    }
                }else{
                    if(stack_local.region == VMemRef.Stack.Region.In){
                        System.out.println("  lw $s" + local_in_index + " " + indexof_stack + "($sp)");
                        local_in_index++;
                    }else if(stack_local.region == VMemRef.Stack.Region.Out){
                        System.out.println("  lw $s" + local_out_index + " " + indexof_stack + "($sp)");
                        local_out_index++;
                    }else if(stack_local.region == VMemRef.Stack.Region.Local){
                        System.out.println("  lw $s" + local_stack_index + " " + indexof_stack + "($sp)");
                        local_stack_index++;
                    }
                }
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
            System.out.println("  " + "bnez " + b.value + " " + branch_target);
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
            /*
            if(!check_if_label_contain(str_value)){
                List<String> temp_list = new ArrayList<String>();
                if(node_label_map.containsKey(p+2)){
                    temp_list.add(str_value);
                    temp_list.addAll(node_label_map.get(p+2));
                    node_label_map.replace(p+2,temp_list);
                }else{
                    temp_list.add(str_value);
                    node_label_map.put(p+2,temp_list);
                }

            }
            */

            //check if the str value is in the label map or vector.
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
