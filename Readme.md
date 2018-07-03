To enter fund details, run the program from command line/console

1. java -jar calculator-0.0.1-SNAPSHOT-jar-with-dependencies.jar / run mainMethod of App.java in Eclipse/Intellij
2. On console: copy-paste/enter (  calculator-0.0.1-SNAPSHOT-jar-with-dependencies.jar is executable jar with all dependencies ) 


	A,B,1000
	A,C,2000
	B,D,500
	B,E,250
	B,F,250
	C,G,1000
	C,H,1000

3. Enter "continue" to terminate scanning. If you need to add more nodes linked to root fund keep entering data and enter. 
4. app will print fund weights of all leaf nodes from portfolio
5. Enter end day market value, if need to compute weighted return. If input fails parsing, app exits.


TestCase:
There is FundOperationsImplTestCase which will test the following operations
1. GetFundWeight against portfolio
2. GetWeightedReturn against end market value
3. GetAllGrandChildrenFunds all leafnodes
