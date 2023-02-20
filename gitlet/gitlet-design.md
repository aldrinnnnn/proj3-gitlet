# Gitlet Design Document
author: Aldrin Sembrana

## Design Document Guidelines

Please use the following format for your Gitlet design document. Your design
document should be written in markdown, a language that allows you to nicely 
format and style a text file. Organize your design document in a way that 
will make it easy for you or a course-staff member to read.  

## 1. Classes and Data Structures

Include here any class definitions. For each class list the instance
variables and static variables (if any). Include a ***brief description***
of each variable and its purpose in the class. Your explanations in
this section should be as concise as possible. Leave the full
explanation to the following sections. You may cut this section short
if you find your document is too wordy.

### Main.java
This class is how we are going to run Gitlet commands, such as initialization,
committing, and adding. This is also where we will implement methods to set up
persistence.

### Commit.java
This class represents the serializable commit objects.
#### Fields
1. String _logMessage: a String representing the accompanying text for whenever a 
commit is made.
2. Date _date: a Date object representing when the commit was made
3. String _hash: a String representing the id of this commit object
4. String _parentHash: a String representing the id of this commit object's parent
5. String _mergeParentHash: a String representing the id of this commit object's 
second parent. 
6. ArrayList<String> _fileNames: an ArrayList of Strings that helps with
referencing blobs, without pointers

### Blob.java
This class represents serializable objects that contain the contents of files.
#### Fields
1. String _name: a String representing the object's file name
2. String _contents: a String representing the written contents of a file

### Branch.java
This class will be in charge of updating pointers as the user inputs gitlet commands.
#### Fields
2. Commit _master: pointer to the most recent commit
3. Commit _head: pointer to the current commit we are working on

### Staging.java
This class represents the staging area
#### Fields
1. ArrayList<String> _addStage: an ArrayList of Strings that represent files
in the staging area
2. ArrayList<String> _remStage: an Arraylist of Strings that represent
the files that wish to be untracked in the next commit

## 2. Algorithms

This is where you tell us how your code works. For each class, include
a high-level description of the methods in that class. That is, do not
include a line-by-line breakdown of your code, but something you would
write in a javadoc comment above a method, ***including any edge cases
you are accounting for***. We have read the project spec too, so make
sure you do not repeat or rephrase what is stated there.  This should
be a description of how your code accomplishes what is stated in the
spec.


The length of this section depends on the complexity of the task and
the complexity of your design. However, simple explanations are
preferred. Here are some formatting tips:

* For complex tasks, like determining merge conflicts, we recommend
  that you split the task into parts. Describe your algorithm for each
  part in a separate section. Start with the simplest component and
  build up your design, one piece at a time. For example, your
  algorithms section for Merge Conflicts could have sections for:

   * Checking if a merge is necessary.
   * Determining which files (if any) have a conflict.
   * Representing the conflict in the file.
  
* Try to clearly mark titles or names of classes with white space or
  some other symbols.

### Main.java
1. main(String[] args): This is the entry point of the program. It 
first checks to make sure that the input array is not empty. Then, 
it calls init() to create the /.gitlet, /.gitlet/commits, 
/.gitlet/staging, /.gitlet/blobs, /.gitlet/branch for persistence. In each directory, 
there will be a file that can be written to or written from using methods
from the Utils class. Lastly, depending on the input argument, different
functions are called to perform the operation.
2. init(): If the necessary directories needed for persistence do not
exist, this command will make them. It will also create the first commit.
3. add(String[] args): This writes the first non-command argument to 
a File object in /.gitlet/staging if the file being referenced is not
there. If the file is already there with no new changes, do nothing. 
If the file is already there with some changes that have not been staged
yet, update changes to existing file. 
4. commit(String[] args): This creates a copy of the most recent commit in 
/.gitlet/commits and updates it according to the files in the staging area 
(for addition and removal). 
5. log(): This command will display the current head commit's ancestor commits
and their information (commit id, time of commit, and commit message).
6. checkout(String[] args): Depending on the arguments, this will do one of 
three things. It can either reference a file in the current head commit, reference
a file from a certain commit, or takes all the files of a commit and brings
it to the working directory.
### Commit.java
1. Commit(String message, String parent): The class constructor.
2. findFile(String fileName): Locates a file within the _fileNames Map and returns it.
3. checkout(String[] args): reverts the current commit to the commit referenced by the id or
takes a specified file or file from head and copies it into the current directory. 
4. viewLog(): returns an organized String of all the previous logs of the head commit

### Blob.java
1. Blob(String name, String contents): The class constructor.

### Branch.java
1. Branch(): The class constructor.
2. updateMaster(): Updates Master pointer instance variable
3. updateHead(): Updates Head pointer instance variable

### Staging.java
1. Staging(): The class constructor, will create new Staging Areas for Addition and Removal.
2. add(String fileName): Determines if fileName should be added to the staging area by 
calling checkContents().
3. remove(String fileName): If fileName is found in the staging area meant for addition, 
it will be placed in the staging area for removal.
4. checkContents(String fileName): returns a boolean for whether fileName's contents in
the current directory is exactly the same as fileName in staging area. 

## 3. Persistence

Describe your strategy for ensuring that you don’t lose the state of your program
across multiple runs. Here are some tips for writing this section:

* This section should be structured as a list of all the times you
  will need to record the state of the program or files. For each
  case, you must prove that your design ensures correct behavior. For
  example, explain how you intend to make sure that after we call
       `java gitlet.Main add wug.txt`,
  on the next execution of
       `java gitlet.Main commit -m “modify wug.txt”`, 
  the correct commit will be made.
  
* A good strategy for reasoning about persistence is to identify which
  pieces of data are needed across multiple calls to Gitlet. Then,
  prove that the data remains consistent for all future calls.
  
* This section should also include a description of your .gitlet
  directory and any files or subdirectories you intend on including
  there.

The main pieces of data needed across multiple calls to Gitlet are 
Commit and Blob objects, the Head and Master pointers, and the
staging area. Calling on `java gitlet.Main init` will be 
needed to maintain persistence as the .gitlet directory gets 
initialized, and this will be where we will keep all the File objects
containing the serializations of contents. To set up 
persistence,
1. Write the Blobs to disk. Each blob can be serialized, and
this serialization can be used as identification for each blob object. 
Each blob will be written to their own file. This can be done with
writeObject method from the Utils class.
2. Write the Commits to disk. Each commit will be serializable and be 
written to their own file. This can be done with writeObject method 
from the Utils class.
3. Write the Branch object to disk to maintain persistent pointers. This 
can be done with writeObject method from the Utils class.
4. Write the Staging Area to disk. After every commit, the staging area
will be modified on the file to be cleared.

To retrieve our state, we verify that the directory that we are currently in
contains all the directories and files needed for persistence. Our file naming
convention will heavily rely on the SHA-1 function, which makes it easy to 
find the files we are looking for since each object will have their own unique
identification. We can use the readObject method from the Utils class to read
the data of the files and deserialize the objects within the files. 



## 4. Design Diagram

Attach a picture of your design diagram illustrating the structure of your
classes and data structures. The design diagram should make it easy to 
visualize the structure and workflow of your program.

