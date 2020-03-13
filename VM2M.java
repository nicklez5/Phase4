import java.util.*;
import cs132.util.ProblemException;
import cs132.vapor.parser.VaporParser;
import cs132.vapor.ast.VaporProgram;
import cs132.vapor.ast.VBuiltIn.Op;
import cs132.vapor.ast.*;
import cs132.vapor.ast.VFunction.Stack;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;

public class VM2M{
    public VaporProgram program_tree;
    public VFunction[] list_functions;
    public VDataSegment[] list_data;
    public Node_Visitor node_visit;
    public List<Map<Integer,String>> label_map;
    public static VaporProgram parseVapor(InputStream in, PrintStream err) throws IOException {
        Op[] ops = {
            Op.Add, Op.Sub, Op.MulS, Op.Eq, Op.Lt, Op.LtS,
            Op.PrintIntS, Op.HeapAllocZ, Op.Error,
        };

        boolean allowLocals = false;
        String[] registers = {
            "v0", "v1",
            "a0", "a1", "a2", "a3",
            "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7",
            "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
            "t8",
        };
        boolean allowStack = true;

        VaporProgram tree;
        try {
            tree = VaporParser.run(new InputStreamReader(in), 1, 1,
            java.util.Arrays.asList(ops),
            allowLocals, registers, allowStack);
        }
        catch (ProblemException ex) {
            err.println(ex.getMessage());
            return null;
        }

        return tree;
    }
    public static void main(String[] args) throws IOException {
        PrintStream ps = new PrintStream("data.txt");
        InputStream x = System.in;
        VaporProgram xyz = parseVapor(x, ps);
        if(xyz != null){
            VM2M godfather = new VM2M(xyz);
            godfather.extract_label_data();
            godfather.big_print_data();
        }
    }

    public VM2M(VaporProgram file_program){
        program_tree = file_program;
        node_visit = new Node_Visitor();
        list_functions = file_program.functions;
        list_data = file_program.dataSegments;
        label_map = new ArrayList< Map<Integer,String> >();


    }
    public void visit_instruction(int k, VInstr temp_instruction){


        if(temp_instruction instanceof VCall){
            node_visit.visit(k,(VCall)temp_instruction);
        }else if(temp_instruction instanceof VAssign){
            node_visit.visit(k,(VAssign)temp_instruction);
        }else if(temp_instruction instanceof VBuiltIn){
            node_visit.visit(k,(VBuiltIn)temp_instruction);
        }else if(temp_instruction instanceof VMemWrite){
            node_visit.visit(k,(VMemWrite)temp_instruction);
        }else if(temp_instruction instanceof VMemRead){
            node_visit.visit(k,(VMemRead)temp_instruction);
        }else if(temp_instruction instanceof VBranch){
            node_visit.visit(k, (VBranch)temp_instruction);
        }else if(temp_instruction instanceof VGoto){
            node_visit.visit(k,(VGoto)temp_instruction);
        }else if(temp_instruction instanceof VReturn){
            node_visit.visit(k,(VReturn)temp_instruction);
        }

    }
    public void print_all_dataSegments(){
        for(int i = 0 ; i < list_data.length ; i++){
            VDataSegment temp_data_segment = list_data[i];
            System.out.println(temp_data_segment.ident + ":");
            VOperand.Static[] da_values = temp_data_segment.values;
            for(int j = 0; j < da_values.length; j++){
                String temp_str = da_values[j].toString();
                String removed_str = temp_str.replace(":","");
                System.out.println("  " + removed_str);
            }
            System.out.println();
        }
    }
    public void etc_print(){
        System.out.println(".data");
        System.out.println("");
        print_all_dataSegments();
        System.out.println(".text");
        System.out.println("");
        System.out.println("  " + "jal Main");
        System.out.println("  " + "li $v0 10");
        System.out.println("  " + "syscall");
        System.out.println("");

    }
    public void entry_to_function(VFunction da_function){
        Stack temp_stack = da_function.stack;
        System.out.println("  " + "sw $fp -8($sp)");
        System.out.println("  " + "move $fp $sp");
        int out_value = temp_stack.out;
        int local_value = temp_stack.local;
        int total_value = local_value + out_value + 2;
        System.out.println("  " + "subu $sp $sp " + total_value * 4);
        System.out.println("  " + "sw $ra -4($fp)");
    }
    public void leave_function(VFunction da_function){
        System.out.println("  " + "lw $ra -4($fp)");
        Stack temp_stack = da_function.stack;
        int out_value = temp_stack.out;
        int local_value = temp_stack.local;
        int total_value = local_value + out_value + 2;
        System.out.println("  " + "lw $fp -8($fp)");
        System.out.println("  " + "addu $sp $sp " + total_value * 4);
        System.out.println("  " + "jr $ra");
    }
    public void extract_label_data(){
        for(int i = 0; i < list_functions.length ; i++){
            VFunction temp_function = list_functions[i];
            Map<Integer,String> line_no_label = new HashMap<Integer,String>();
            for(int xyz = 0; xyz < temp_function.labels.length; xyz++){
                VCodeLabel code_label = temp_function.labels[xyz];
                line_no_label.put(code_label.instrIndex,code_label.ident);
            }
            label_map.add(line_no_label);
        }
    }
    public void save_stack(VFunction da_function){
        Stack temp_stack = da_function.stack;
        if(temp_stack.local > 0){
            for(int i = 0; i < temp_stack.local;i++){
                int stack_ptr = i * 4;
                System.out.println("  " + "sw $s" + i + " " + stack_ptr + "($sp)");
            }
        }
    }
    public void restore_stack(VFunction da_function){
        Stack temp_stack = da_function.stack;
        if(temp_stack.local > 0){
            for(int i = 0; i < temp_stack.local;i++){
                int stack_ptr = i * 4;
                System.out.println("  " + "lw $s" + i + " " + stack_ptr + "($sp)");
            }
        }
    }
    public void ending_print(){
        System.out.println("_print:");
        System.out.println("  " + "li $v0 1   # syscall: print integer");
        System.out.println("  " + "syscall");
        System.out.println("  " + "la $a0 _newline");
        System.out.println("  " + "li $v0 4   # syscall: print string");
        System.out.println("  " + "syscall");
        System.out.println("  " + "jr $ra");
        System.out.println("");
        System.out.println("_error:");
        System.out.println("  " + "li $v0 4   # syscall: print string");
        System.out.println("  " + "syscall");
        System.out.println("  " + "li $v0 10  # syscall: exit");
        System.out.println("  " + "syscall");
        System.out.println("");
        System.out.println("_heapAlloc:");
        System.out.println("  " + "li $v0 9   # syscall: sbrk");
        System.out.println("  " + "syscall");
        System.out.println("  " + "jr $ra");
        System.out.println("");
        System.out.println(".data");
        System.out.println(".align 0");
        System.out.println("_newline: .asciiz \"\\n\"");
        System.out.println("_str0: .asciiz \"null pointer\\n\"");

    }
    public void big_print_data(){
        etc_print();
        for(int i = 0 ; i < list_functions.length ; i++){
            VFunction temp_function = list_functions[i];
            System.out.println(temp_function.ident + ":");
            entry_to_function(temp_function);
            save_stack(temp_function);
            Map<Integer,String> current_label_map = label_map.get(i);
            int current_t = 0;
            for(int xyz_2 = 0; xyz_2 < temp_function.body.length; xyz_2++){
                VInstr instruction = temp_function.body[xyz_2];

                if(current_label_map.containsKey(xyz_2)){
                    System.out.println(current_label_map.get(xyz_2) + ":");
                }
                visit_instruction(xyz_2,instruction);

            }
            restore_stack(temp_function);
            leave_function(temp_function);

            System.out.println("");
        }
        ending_print();
    }


}
