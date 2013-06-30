package apps;

import huffman.AdaptiveHuffmanModel;
import huffman.CodePair;

import java.awt.AlphaComposite;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import bitstreams.BitInputStream;

abstract public class Application 
{
	public static void main(String[] args) 
	{
		char alphabet[] = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','\0'};
		
		AdaptiveHuffmanModel model = new AdaptiveHuffmanModel(alphabet);
		try 
		{
			model.encode(new FileInputStream("input.txt"), 
					 new FileOutputStream("compressed.bin"));
			model.reset();
			
			model.decode(new FileInputStream("compressed.bin"), 
					 new FileOutputStream("decompressed.txt") );
			
			model.reset();
			debug(	model,
					new FileInputStream("compressed.bin"), 
					new FileOutputStream("compressed.txt") );
		} 
		catch (FileNotFoundException e) {e.printStackTrace();} 
		catch (IOException e) {e.printStackTrace();}
	}
	
	public static void debug( AdaptiveHuffmanModel model, InputStream inStream, OutputStream outStream )
	{
		BitInputStream input = new BitInputStream(inStream);
		PrintWriter outWriter = new PrintWriter(new OutputStreamWriter(outStream));
		
		try 
		{
			outWriter.println("debug output:");
			outWriter.println();
			outWriter.println("alphabet length:"+model.alphabet.length);
			outWriter.println("selected e="+model.e+" ; ( =log_2(alphabet.length) )");
			outWriter.println("selected r="+model.r+";  ( =alphabet.length-2^e )");
			
			outWriter.println();
			outWriter.println("character selected for end of input: "+model.alphabet[model.alphabet.length-1]+
							" (value="+(int)model.alphabet[model.alphabet.length-1]+")");
			
			outWriter.println("((last character in the alphabet))");
			outWriter.println();
			
			outWriter.println("default codes for alphabet using e and r:");
			outWriter.println("letter(value):\t dafault code");
			
			outWriter.println("-------------------------------");
			
			for( char c : model.alphabet )
			{
				CodePair p = model.getDefaultCode(c);
				outWriter.println(c+"("+(int)c+"):\t "+String.format("%"+p.length+"s", Integer.toBinaryString(p.code)).replace(' ', '0'));
				
			}
			
			outWriter.println();
			outWriter.println();
			outWriter.println();
			outWriter.println(" the compressed message was (may contain padding bits in the end of the message): ");
			outWriter.println();
			
			int c = 0;
			while( (c = input.read(1)) != -1 )
			{
				outWriter.write(Integer.toBinaryString(c));
			}
			input.close();
			outWriter.close();
		} 
		catch (IOException e) {e.printStackTrace();}
	}
}
