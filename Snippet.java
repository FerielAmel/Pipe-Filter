import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Queue;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import java.io.*;

import java.util.Map;

import java.util.HashMap;

import java.util.Stack;

import java.util.Scanner;

/***********************************************Main*********************************************/

public class Snippet {
	public static void main(String[] args) {
		Pipe p1 = new BlockingQueue();
		Pipe p2 = new BlockingQueue();
		Pipe p3 = new BlockingQueue();
		CalculatorFilter calculator = new CalculatorFilter(p3, p1);
		InterpreterFilter interpreter = new InterpreterFilter(p1, p2);
		TraceFilter trace = new TraceFilter(p2, p3);
		
		Thread th2 = new Thread(calculator);
		Thread th1 = new Thread(interpreter);
		Thread th3 = new Thread(trace);
		th2.start();
		th1.start();
		th3.start();
		

	}

}


/***************************************PIPE & FILTER************************************************/

class Pipe {
    private String data;

    public synchronized void dataIN(String data) {
        this.data = data;
        notify(); // Notify any waiting threads that new data is available
    }

    public synchronized String dataOUT() {
        while (data == null) {
            try {
                wait(); // Wait for new data to be available
            } catch (InterruptedException e) {
                // Handle interrupted exception
            }
        }
        String output = data;
        data = null;
        return output;
    }
}


abstract class Filter implements Runnable{
	Pipe _dataINPipe = new Pipe();
	Pipe _dataOUTPipe = new Pipe();

	public String getData(){
		return _dataINPipe.dataOUT();
	}

	public void sendData(String tempData){
		_dataOUTPipe.dataIN(tempData);
	}

	abstract void execute();
	@Override
	public void run() {
		execute();
	}
}



class BlockingQueue extends Pipe {
	Queue<String> _inData = new LinkedList<String>();

	public synchronized void dataIN (String in){
		_inData.add(in);
		notify();
	}

	public synchronized String dataOUT (){
		if(_inData.isEmpty())
		try {

			wait();

		} catch (InterruptedException e) {

		// TODO Auto-generated catch block
		e.printStackTrace();

	}

	return _inData.poll();
	}

}


/******************************************Filtre: GUI*********************************************************/

class CalculatorFilter extends Filter implements ActionListener {
    private JTextField numField1, numField2, resultField;

    public CalculatorFilter(Pipe _dataINPipe, Pipe _dataOUTPipe) {
        super();
        this._dataINPipe = _dataINPipe;
        this._dataOUTPipe = _dataOUTPipe;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Sum")) {
            try {
                int num1 = Integer.parseInt(numField1.getText());
                int num2 = Integer.parseInt(numField2.getText());
                String result = String.valueOf(num1) +" "+ String.valueOf(num2)+" +";
                sendData(result);
                resultField.setText(getData());
                /*resultField.setText(); GET DATA FROM PIPE*/
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid input: Please enter integers only.");
            }
        }
        
        if  (e.getActionCommand().equals("Multiply")) {
            try{
	    int num1 = Integer.parseInt(numField1.getText());
	    int num2 = Integer.parseInt(numField2.getText());
	    String result = String.valueOf(num1) +" "+ String.valueOf(num2)+" *";
            sendData(result);
            resultField.setText(getData());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid input: Please enter integers only.");
            }
	}
	if  (e.getActionCommand().equals("Factorial")) {
	    int num1 = Integer.parseInt(numField1.getText());
	    String result = String.valueOf(num1) +" 0 !";
            sendData(result);
            resultField.setText(getData());
	}
	if  (e.getActionCommand().equals("Trace")) {
	    String result = "/ / / /";
            sendData(result);
            resultField.setText(getData());
	}

    }

    synchronized void execute() {
		// Set up the frame
		JFrame frame = new JFrame("Calculator Filter");
		frame.setSize(300, 150);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Set up the layout
		frame.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;

		// Add the components
		JLabel label1 = new JLabel("Number 1:");
		c.gridx = 1;
		c.gridy = 0;
		frame.add(label1, c);

		numField1 = new JTextField(10);
		c.gridx = 2;
		c.gridy = 0;
		frame.add(numField1, c);

		JLabel label2 = new JLabel("Number 2:");
		c.gridx = 1;
		c.gridy = 1;
		frame.add(label2, c);

		numField2 = new JTextField(10);
		c.gridx = 2;
		c.gridy = 1;
		frame.add(numField2, c);

		JLabel resultLabel = new JLabel("Result:");
		c.gridx = 1;
		c.gridy = 2;
		frame.add(resultLabel, c);

		resultField = new JTextField(10);
		resultField.setEditable(false);
		c.gridx = 2;
		c.gridy = 2;
		frame.add(resultField, c);

		JButton sumButton = new JButton("Sum");
		sumButton.addActionListener(this);
		c.gridx = 1;
		c.gridy = 4;
		frame.add(sumButton, c);
		
		
		// Add the Multiply button
		JButton multiplyButton = new JButton("Multiply");
		multiplyButton.addActionListener(this);
		c.gridx = 2;
		c.gridy = 4;
		frame.add(multiplyButton, c);

		// Add the Factorial button
		JButton factorialButton = new JButton("Factorial");
		factorialButton.addActionListener(this);
		c.gridx = 3;
		c.gridy = 4;
		frame.add(factorialButton, c);
		
		// Add the Trace button
		JButton traceButton = new JButton("Trace");
		traceButton.addActionListener(this);
		c.gridx = 2;
		c.gridy = 6;
		frame.add(traceButton, c);
		
	
		

		// Show the frame
		frame.setVisible(true);
	    }
	
}


/**********************************************Filter: Caclculator******************************************************/


class InterpreterFilter extends Filter{
	
	public InterpreterFilter(Pipe _dataINPipe, Pipe _dataOUTPipe) {
		super();
		this._dataINPipe = _dataINPipe;
		this._dataOUTPipe = _dataOUTPipe;
    	}
    	
    	synchronized void execute() {
		while (true){
			String expression = getData();
			/*Receive data and parse*/
			String [] numbers = expression.split(" ");
			if (numbers[1].equals("/")){
				sendData(expression);
			}
			else{
				String formula = "x y "+ numbers[2]; /*Operator*/
				Evaluator exp = new Evaluator(formula);

				Map<String, Expression> variables = new HashMap<String, Expression>();
				
				variables.put(Character.toString(formula.charAt(0)), new Number(Integer.valueOf(numbers[0])));
				variables.put(Character.toString(formula.charAt(2)), new Number(Integer.valueOf(numbers[1])));
				int result = exp.interpret(variables);
				String final_result = numbers[0]+" "+numbers[2]+" "+numbers[1]+" "+Integer.toString(result);
				sendData(final_result);
			}
			
			
		}

        }
}






class Evaluator implements Expression {

    private Expression syntaxTree;



    public Evaluator(String expression) {
    

        Stack<Expression> expressionStack = new Stack<Expression>();

        for (String token : expression.split(" ")) {

            if (token.equals("+")) {

                Expression subExpression = new Plus(expressionStack.pop(), expressionStack.pop());

                expressionStack.push(subExpression);

            } else if (token.equals("-")) {

                Expression right = expressionStack.pop();

                Expression left = expressionStack.pop();

                Expression subExpression = new Minus(left, right);

                expressionStack.push(subExpression);

            } else if (token.equals("*")) {

                Expression right = expressionStack.pop();

                Expression left = expressionStack.pop();

                Expression subExpression = new Multiply(left, right);

                expressionStack.push(subExpression);

            }else if (token.equals("!")) {

                Expression right = expressionStack.pop();

                Expression left = expressionStack.pop();

                Expression subExpression = new Factorial(left, right);

                expressionStack.push(subExpression);

            }
            else

                expressionStack.push(new Variable(token));

        }

        syntaxTree = expressionStack.pop();

    }



    public int interpret(Map<String, Expression> context) {

        return syntaxTree.interpret(context);

    }

}



interface Expression {

    public int interpret(Map<String, Expression> variables);

}



class Number implements Expression {

    private int number;



    public Number(int number) {

        this.number = number;

    }



    public int interpret(Map<String, Expression> variables) {

        return number;

    }

}



class Plus implements Expression {

    Expression leftOperand;

    Expression rightOperand;



    public Plus(Expression left, Expression right) {

        leftOperand = left;

        rightOperand = right;

    }



    public int interpret(Map<String, Expression> variables) {

        return leftOperand.interpret(variables) +

                rightOperand.interpret(variables);

    }

}




class Multiply implements Expression {

    Expression leftOperand;

    Expression rightOperand;



    public Multiply(Expression left, Expression right) {

        leftOperand = left;

        rightOperand = right;

    }



    public int interpret(Map<String, Expression> variables) {

        return leftOperand.interpret(variables) *

                rightOperand.interpret(variables);

    }

}



class Factorial implements Expression {

    Expression leftOperand;

    Expression rightOperand;



    public Factorial(Expression left, Expression right) {

        leftOperand = left;

        rightOperand = right;

    }



    public int interpret(Map<String, Expression> variables) {
    	if (leftOperand.interpret(variables) == 0 || leftOperand.interpret(variables) == 1)
    	{
    		return 1;
    	}
    	else{
    	
    		int n = leftOperand.interpret(variables) ;
        	int result = 1;
        	for (int i = 2; i <= n; i++) {
            		result *= i;
        	}
        	return result;

	}

	}
}




class Minus implements Expression {

    Expression leftOperand;

    Expression rightOperand;



    public Minus(Expression left, Expression right) {

        leftOperand = left;

        rightOperand = right;

    }



    public int interpret(Map<String, Expression> variables) {

        return leftOperand.interpret(variables) -

                rightOperand.interpret(variables);

    }

}



class Variable implements Expression {

    private String name;



    public Variable(String name) {

        this.name = name;

    }


    public int interpret(Map<String, Expression> variables) {

        if (null == variables.get(name))

            return 0;

        return variables.get(name).interpret(variables);

    }

}



/******************************************Filter: Trace****************************************************************/




class TraceFilter extends Filter {
 
    Pipe _dataINPipe;
    Pipe _dataOUTPipe;
     
    public TraceFilter (Pipe _dataINPipe, Pipe _dataOUTPipe) {
		super();
		this._dataINPipe = _dataINPipe;
		this._dataOUTPipe = _dataOUTPipe;
	}
    
    public String getData(){
        return _dataINPipe.dataOUT();
    }
     
    public void sendData( String tempData){
        _dataOUTPipe.dataIN(tempData);
    }

    @Override
	public void run() {
		// TODO Auto-generated method stub
		execute();
	}
	@Override
	synchronized void execute() {
		// TODO Auto-generated method stub
		while (true) {
			String data = getData();
			String opt = "";
			String out = "";
			String[] result = new String[4];
			result = data.split(" ");
			opt = result[1];
			
			switch (opt){
				case "+"://sum
				case "*"://product
					out = result[0]+result[1]+result[2]+" = "+result[3];
					try {
						BufferedWriter writer = new BufferedWriter(new FileWriter("trace.txt", true));
						writer.append('\n');
						writer.append(out);
						writer.close();
					}  catch (IOException e) {
						e.printStackTrace();
					}
					out = result[3];
					break;
				case "!":// facto
					int f = 1;
					out = result[0]+"!"+" = "+result[3];
					try {
						BufferedWriter writer = new BufferedWriter(new FileWriter("trace.txt", true));
						writer.append('\n');
						writer.append(out);
						writer.close();
					}  catch (IOException e) {
						e.printStackTrace();
					}
					out = result[3];
					break;
				case "/"://trace
					try {
						Scanner read = new Scanner(new File("trace.txt"));
						while (read.hasNextLine()) {
							out = "Trace : " + read.nextLine();
						}
						read.close();


					}  catch (IOException e) {
						e.printStackTrace();
					}
					break;
				default:
					break;
			}



			sendData(out);
		}
	}
}





