package huffman;

public class Node
{
	private Node left;
	private Node right;
	private Node parent;
	private int weight;
	private int nodeNumber;

	public Node()
	{
		this.left = null;
		this.right = null;
		this.parent = null;
		this.weight = 0;
		this.nodeNumber = 0;
	}
	
	public Node(int nodeNumber) 
	{
		this.left = null;
		this.right = null;
		this.parent = null;
		this.weight = 0;
		this.nodeNumber = nodeNumber;
	}

	public Node getLeft()
	{
		return left;
	}

	public void setLeft(Node left) 
	{
		if(left.getParent()!=null)
			throw new IllegalArgumentException("candidate for left already has a parent");
		
		this.left = left;
		left.parent = this;
	}

	public Node getRight()
	{
		return right;
	}

	public void setRight(Node right)
	{
		if(right.getParent()!=null)
			throw new IllegalArgumentException("candidate for right already has a parent");
		
		this.right = right;
		right.parent = this;
	}

	public int getWeight()
	{
		return weight;
	}

	public void setWeight(int weight)
	{
		this.weight = weight;
	}
	
	public void incrementWeight()
	{
		this.weight++;
	}
	
	public int getNodeNumber() 
	{
		return nodeNumber;
	}

	public void setNodeNumber(int nodeNumber)
	{
		this.nodeNumber = nodeNumber;
	}
	
	public Node getParent()
	{
		return parent;
	}
	
	public Node detachLeft()
	{
		Node left = this.left;
		this.left = null;
		left.parent = null;
		return left;
	}
	
	public Node detachRight()
	{
		Node right = this.right;
		this.right = null;
		right.parent = null;
		return right;
	}
	
	public void detachFromParent()
	{
		if(this.parent==null)
			return;
		
		if( this.parent.left==this )
			this.parent.detachLeft();
		else
			this.parent.detachRight();
	}
	
	public boolean isLeftChild()
	{
		if( this.parent==null )
			return false;
		
		return (this.parent.left == this);
	}
	
	public boolean isRightChild()
	{
		if( this.parent==null )
			return false;
		
		return (this.parent.right == this);
	}
	
	public boolean isLeaf()
	{
		return (this.left==null && this.right==null);
	}
}
