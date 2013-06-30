package huffman;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

import bitstreams.BitInputStream;
import bitstreams.BitOutputStream;

public class AdaptiveHuffmanModel
{
//	TODO check if alphabetIndexes actually improves performance over simple iteration
	
	private Node root;
	private Node notYetTransmitted;
	private HashMap<Character, Integer> alphabetIndexes;	// for O(1) search for character index in alphabet
	private HashMap<Character, Node> transmitted;			// for O(1) search for node by character
	private HashMap<Node, Character> charByNode;			// for O(1) search for character by node
	private ArrayList<Node> nodes;							// for O(n) iterating while searching for weight block
	
	
	/**
	 * alphabet for this model
	 */
	public final char alphabet[];

	/**
	 * e and r are used for default codes <br>
	 * constraint: alphabet length = 2^e + r
	 */
	public final int e;
	
	/**
	 * e and r are used for default codes <br>
	 * constraint: alphabet length = 2^e + r
	 */
	public final int r;
	
	//-----------------------------------------------------------
	//TEMP TEMP TEMP TEMP TEMP TEMP TEMP TEMP TEMP TEMP TEMP TEMP
	public Node getRoot() {
		return root;
	}
	//TEMP TEMP TEMP TEMP TEMP TEMP TEMP TEMP TEMP TEMP TEMP TEMP
	//-----------------------------------------------------------
	
	/**
	 * construct a new model using given 
	 * @param alphabet the alphabet for this model 
	 * @note IMPORTANT! LAST LETTER IN GIVEN ALPHABET WILL BE USED AS END OF INPUT!
	 */
	public AdaptiveHuffmanModel( char alphabet[] )
	{
		if( alphabet==null )
			throw new NullPointerException("cannot accept null alphabet");
		
		e = (int) (Math.log(alphabet.length) / Math.log(2));
		r = alphabet.length - (int)Math.pow(2,e);
		this.alphabet = alphabet;
		
		alphabetIndexes = new HashMap<Character, Integer>();
		
		for( int i=0 ; i<alphabet.length ; i++ )
		{
			alphabetIndexes.put(alphabet[i], i);
		}
		
		reset();
	}
	
	
	/**
	 * resets the model - removes all memory of past transmissions.
	 */
	public void reset()
	{
		this.notYetTransmitted = new Node(alphabet.length*2 - 1);
		this.root = this.notYetTransmitted;
		this.transmitted = new HashMap<Character, Node>();
		this.charByNode = new HashMap<Node, Character>();
		this.nodes = new ArrayList<Node>();
		this.nodes.add(this.root);
	}
	
	
	/**
	 * update the model
	 * @param c the next character for transmission
	 */
	public void update(char c)
	{
		Integer index = alphabetIndexes.get(c);
		
		if(index==null)
			throw new IllegalArgumentException("character '"+c+"' (code="+(int)c+") is not a part if the alphabet");
		
		Node n = transmitted.get(c);
		
		if( n==null ) // first appearance of the character
		{
			n = splitNYT(c);
			
			if( n==root )
				return;
			else
				n = n.getParent();
		}
		else if( n.getParent()==this.notYetTransmitted.getParent() )
		{
			Node highest = findMaximumLeafInBlock(n.getWeight());
			
			if( n!=highest )
				swapNodes(n, highest);
			
			n.incrementWeight();
			n = n.getParent();
		}
		
		// climb up and update route's weight
		// swap elements when needed
		while( n!=root )
		{
			Node highest = findMaximumNodeInBlock(n.getWeight());
			
			if( n!=highest )
				swapNodes(n, highest);
			
			n.incrementWeight();
			n = n.getParent();
		}
		n.incrementWeight();
	}
	
	/**
	 * will split the nyt node <br>
	 * will set the new nyt node as the left child of the old nyt<br>
	 * will set the new external node as the right child of the old node <br>
	 * will register transmission in 'transmitted' hash map :<br>
	 * key:character 'c' , value: the new external node
	 * @param c the new character
	 * @return the old NYT node (the one that was split)
	 */
	private Node splitNYT(char c)
	{
		Node newNYT = new Node(this.notYetTransmitted.getNodeNumber()-2);
		Node newExternal = new Node(this.notYetTransmitted.getNodeNumber()-1);
		
		this.notYetTransmitted.setLeft(newNYT);
		this.notYetTransmitted.setRight(newExternal);
		
		Node oldNYT = this.notYetTransmitted;
		this.notYetTransmitted = newNYT;
		
		oldNYT.incrementWeight();
		newExternal.incrementWeight();
		
		// register nodes
		this.transmitted.put(c, newExternal);
		this.charByNode.put(newExternal, c);
		this.nodes.add(newExternal);
		this.nodes.add(newNYT);

		return oldNYT;
	}
	
	/**
	 * will search for the node with the highest node number who's weight equals 'weight'
	 * @param weight the block's weight
	 * @return the node found
	 */
	private Node findMaximumNodeInBlock( int weight )
	{
		Node maxnode = null;
		for( Node n : nodes )
		{
			if(n!=root)
			{
				if( maxnode==null && n.getWeight()==weight ) 
					maxnode = n;
				else if( n.getWeight()==weight )
				{
					if( maxnode.getNodeNumber() < n.getNodeNumber() )
						maxnode = n;
				}
			}
		}
		
		return maxnode;
	}
	
	/**
	 * will search for the leaf with the highest node number who's weight equals 'weight'
	 * @param weight the block's weight
	 * @return the node found
	 */
	private Node findMaximumLeafInBlock( int weight )
	{
		Node maxnode = null;
		for( Node n : nodes )
		{
			if(n!=root && n.isLeaf())
			{
				if( maxnode==null && n.getWeight()==weight ) 
					maxnode = n;
				else if( n.getWeight()==weight )
				{
					if( maxnode.getNodeNumber() < n.getNodeNumber() )
						maxnode = n;
				}
			}
		}
		
		return maxnode;
	}
	
	/**
	 * switch nodes location in the model
	 * @param a first node
	 * @param b second node
	 */
	private void swapNodes( Node a, Node b )
	{
		Node parentOf_a = a.getParent();
		Node parentOf_b = b.getParent();
		
		boolean aWasLeft = a.isLeftChild();
		boolean bWasLeft = b.isLeftChild();
		
		a.detachFromParent();
		b.detachFromParent();
		
		if( aWasLeft )
			parentOf_a.setLeft(b);
		else
			parentOf_a.setRight(b);
		
		if( bWasLeft )
			parentOf_b.setLeft(a);
		else
			parentOf_b.setRight(a);
		
		int temp = a.getNodeNumber();
		a.setNodeNumber(b.getNodeNumber());
		b.setNodeNumber(temp);
	}
	
	/**
	 * return code for character 'c' according to the model's current state
	 * @param c 
	 * @return 
	 */
	public CodePair getCode(char c)
	{
		Node n = transmitted.get(c);
		
		CodePair p = null;
		
		if(n==null)
		{
			p = getDefaultCode(c);
			n = this.notYetTransmitted;
		}
		else
			p = new CodePair();
		
		while( n!=root )
		{
			if(n.isRightChild())
				p.code |= 1<<p.length;
			
			p.length++;
			
			n = n.getParent();
		}
		
		return p;
	}
	
	
	/**
	 * get the default code for character c (NYT)
	 * @param c character
	 * @return a CodePair
	 */
	public CodePair getDefaultCode(char c)
	{
		Integer index = alphabetIndexes.get(c);
		
		if(index==null)
			throw new IllegalArgumentException("character '"+c+"' ("+(int)c+") is not a part if the alphabet");
		
		CodePair p = new CodePair();
		
		if( index < this.r*2 )
		{
			p.code = index;
			p.length = this.e + 1;
		}
		else
		{
			p.code = index - this.r;
			p.length = this.e;			
		}
		return p;
	}
	
	/**
	 * decode input using adaptive Huffman coding algorithm.
	 * @param inStream input to decode
	 * @param outStream stream to write to
	 * @throws IOException
	 */
	public void decode( InputStream inStream, OutputStream outStream ) throws IOException
	{
		BitInputStream input = new BitInputStream(inStream);
		BufferedWriter outWriter = new BufferedWriter(new OutputStreamWriter(outStream));
		
		Node curTravesal = this.root;
		int bit = 0;
		
		while( bit!=-1 )
		{
			if( curTravesal.isLeaf() )
			{
				char c ;
				if( curTravesal==this.notYetTransmitted )
				{
					int b = input.read(this.e);
					
					if( b<this.r )
						b = b<<1 | input.read(1);
					
					else
						b += this.r;
					
					c = this.alphabet[b];
				}
				else
					c = this.charByNode.get(curTravesal);
				
				if(c == alphabet[alphabet.length-1] )
					break;
				
				outWriter.write(c);
				this.update(c);
				curTravesal = this.root;
			}
			
			bit = input.read(1);
			
			if(bit==0)
				curTravesal = curTravesal.getLeft();
			else
				curTravesal = curTravesal.getRight();
		}
		
		input.close();
		outWriter.close();
	}
	
	/**
	 * will encode plain text using adaptive Huffman coding algorithm <br>
	 * @param plaintext text for encoding
	 * @param stream output stream to write to
	 * @throws IOException 
	 */
	public void encode(InputStream inStream, OutputStream outStream) throws IOException
	{
		BitOutputStream output = new BitOutputStream(outStream);
		BufferedReader input = new BufferedReader( new InputStreamReader(inStream) );
		
		char c;
		while( (c=(char) input.read()) != 65535 )
		{
			CodePair p = this.getCode(c);
			output.write(p.length, p.code);
			this.update(c);
		}
		
		//write end of input
		CodePair p = this.getCode(alphabet[alphabet.length-1]);
		output.write(p.length, p.code);
		this.update(alphabet[alphabet.length-1]);
		
		input.close();
		output.close();
	}
}
