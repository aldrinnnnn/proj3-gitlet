package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author aldrin
 */
public class Main {
    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        Initialize initializer = new Initialize();
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        } else if (args[0].equals("init")) {
            initializer.init();
        } else if (args[0].equals("add")) {
            initializer.add(args[1]);
        } else if (args[0].equals("rm")) {
            initializer.rm(args[1]);
        } else if (args[0].equals("commit")) {
            if (args.length == 1 || args[1].equals("")) {
                System.out.println("Please enter a commit message.");
                System.exit(0);
            }
            initializer.commit(args[1]);
        } else if (args[0].equals("log")) {
            initializer.log();
        } else if (args[0].equals("global-log")) {
            initializer.global();
        } else if (args[0].equals("checkout")) {
            if (args.length == 3) {
                initializer.checkout(args[1], args[2]);
            }
            if (args.length == 4) {
                if (!args[2].equals("--")) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                initializer.checkout(args[1], args[2], args[3]);
            }
            if (args.length == 2) {
                initializer.checkout(args[1]);
            }
        } else if (args[0].equals("find")) {
            initializer.find(args[1]);
        } else if (args[0].equals("branch")) {
            initializer.branch(args[1]);
        } else if (args[0].equals("rm-branch")) {
            initializer.rmBranch(args[1]);
        } else if (args[0].equals("status")) {
            initializer.status();
        } else if (args[0].equals("reset")) {
            initializer.reset(args[1]);
        } else if (args[0].equals("merge")) {
            initializer.merge(args[1]);
        } else {
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
    }
}
