# SequenceAssembler (version alpha)

This is the Java code for the application behind my M.Sc. thesis in Information Technology, "Sequence Assembler for Digital Storytelling".
Sequence Assembler takes some text documents (something we refer to as Fragments) and two parameters of Window Size (W) and Match Size (M) as its inputs, and the output is what we call the "Overall Story", resulted by merging those fragments.

Other libraries/JARs which must be imported to the project, but are not included in the repository are:

* stanford-corenlp-3.3.1.jar
* stanford-corenlp-3.3.1-javadoc.jar
* stanford-corenlp-3.3.1-models.jar
* stanford-corenlp-3.3.1-sources.jar
* jaws-bin.jar (WordNet library)

Note:

1) First 4 stanford CoreNLP libraries are available at http://stanfordnlp.github.io/CoreNLP/download.html
2) The last one, WordNet dictionary is available at https://wordnet.princeton.edu/wordnet/download/
3) Import .jar files of all these libraries as external library to the NetBeans' project


How to Use the Application:

1) Choose some integer for Window Size and Match Size
2) While entering the inputs keep in mind that M must always be smaller that W.
3) Browse for some text files (*.txt) to import them as input
4) Press Start


If you are studying in University of Eastern Finland and are looking for more information regarding this applicaiton,
please refer to my IT project "Sequence Assembler for Digital Storytelling" written by me, Mohsen Nasiri on Autumn of 2015.

Any usage of this code with the purpose of education or futhur development is allowed. For other purposes please send an Email to me at mohsen.n89@gmail.com.
