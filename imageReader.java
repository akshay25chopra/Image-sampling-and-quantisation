
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;


public class imageReader {

	JFrame frame;
	JLabel lbIm1;
	JLabel lbIm2;
	BufferedImage img;
	BufferedImage imgmod;
	

	public void showIms(String[] args){
		int width = 352; 
		int height = 288; 
		

		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		imgmod = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		try {
			File file = new File(args[0]);
			InputStream is = new FileInputStream(file);

			long len = file.length();
			byte[] bytes = new byte[(int)len];
			double[] bytes2 = new double[(int) len];
			double[] sampled = new double[(int) len];
			double[] bytes3 = new double[(int) len];
			

			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
				offset += numRead;
			}


			int ind = 0;
			for(int z= 0; z < height; z++){

				for(int x = 0; x < width; x++){

					byte a = 0;
					int r = bytes[ind] & 0xff;
					int g = bytes[ind+height*width] & 0xff;
					int b =  bytes[ind+height*width*2]& 0xff; 
					
					
					
					double y = (double)(r*0.299 + g*0.587 + b*0.114);
					double u = (double)(r*0.596 + g*(-0.274) + b*(-0.322));
					double v = (double)(r*0.211 + g*(-0.523) + b*0.312);
					
					bytes2[ind] = y;
					bytes2[ind+height*width] = u;
					bytes2[ind+height*width*2] = v;
					

					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					img.setRGB(x,z,pix);
					ind++;
				}
			}
			
			int ysub = Integer.parseInt(args[1]);
			int usub = Integer.parseInt(args[2]);
			int vsub = Integer.parseInt(args[3]);
			
			//Subsampling the Y Subspace
			int p1=0;
			for(int q=0;q<width*height;q++)
			{	if(ysub == 0)
					sampled[q] = 0;
			else{
					if(p1==q)
					{
						sampled[q] = bytes2[q];
						p1 = p1 + ysub;
					
					}
					else
					sampled[q] = -1000;
				}
			}
			
			
			//Subsampling the U subspace
			int p2=width*height;
			for(int q=width*height;q<width*height*2;q++)
			{	if(usub == 0)
					sampled[q] = 0;
				if(p2==q)
				{
					sampled[q] = bytes2[q];
					p2 = p2 + usub;
					
				}
				else
					sampled[q] = -1000;
			}
		
			
			//Subsampling the V Subspace
			int p3=width*height*2;
			for(int q=width*height*2;q<sampled.length;q++)
			{	if(vsub ==0)
					sampled[q] = 0;
			else{
					if(p3==q)
					{
						sampled[q] = bytes2[q];
						p3 = p3 + vsub;
					
					}
					else
					sampled[q] = -1000;
				}
				
			}
			
		
			
			
			//Entering Missing values for the Y Subspace
			for(int q=0;q<height*width;++q)
			{	if(sampled[q] == -1000)
								
					if(ysub == 2)
						sampled[q] = ((sampled[q+1]+sampled[q-1])/2); 
					
					else
						sampled[q] = sampled[q-1];
				
			}
			
			// Entering Missing values for the U Subspace
			for(int q=height*width;q<height*width*2;++q)
			{
				if(sampled[q] == -1000)
					
					if(usub == 2)
						sampled[q] = ((sampled[q+1]+sampled[q-1])/2); 
					
					else
						sampled[q] = sampled[q-1];
				
			}
			
			
			//Entering Missing Values for the V Subspace
			for(int q=height*width*2;q<sampled.length;++q)
			{
				if(sampled[q] == -1000)
				
					if(q == sampled.length -1)
						sampled[q] = sampled[q-1];
					else if(vsub == 2)
						sampled[q] = ((sampled[q+1]+sampled[q-1])/2); 
					
					else
						sampled[q] = sampled[q-1];
					
			}
			
			
			
			//Converting back to RGB Space
			int d=0;
			for(int z= 0; z < height; z++){

				for(int x = 0; x < width; x++){

					
					double ycap =  sampled[d];
					double ucap = sampled[d+height*width];
					double vcap = sampled[d+height*width*2]; 
					
					double rcap = (double)(ycap*1 + ucap*0.956 + vcap*0.621);
					double gcap = (double)(ycap*1 + ucap*(-0.272) + vcap*(-0.647));
					double bcap = (double)(ycap*1 + ucap*(-1.106) + vcap*1.703);
					
					
					bytes3[d] = rcap;
					bytes3[d+height*width] =  gcap;
					bytes3[d+height*width*2] = bcap;
					
					
					d++;
					
					
				}
			}
			
			for(int k=0;k<bytes3.length;k++)
			{	
				if(bytes3[k]<0)
					bytes3[k] = 0;
				else if(bytes3[k]>255)
					bytes3[k] = 255;
			
			}
			
			//Quantisation
			int quant = Integer.parseInt(args[4]);
			int qu = (256/quant);
			 
			int arr[] = new int[quant];
			arr[0] = 0;
			for(int c=1;c<quant;c++)
				arr[c] = arr[c-1] + qu;
			
			
			for(int l=0;l<bytes3.length;l++)
			{	for(int k=quant-1;k>=0;k--)
				{if(bytes3[l]>arr[k])
					{bytes3[l] = (int) arr[k];
					break;
					}
				}
			}	
			
			
			//Displaying new image
			int in =0;
			for(int z= 0; z < height; z++){

				for(int x = 0; x < width; x++){

					byte a = 0;
					byte r2 = (byte) bytes3[in];
					byte g2 = (byte) bytes3[in+height*width];
					byte b2 = (byte) bytes3[in+height*width*2]; 
			

					int pix2 = (0xff000000 | ((r2 & 0xff) << 16) | ((g2 & 0xff) << 8) | (b2 & 0xff))  ;
					//System.out.println(pix2);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
					imgmod.setRGB(x,z,pix2);
					in++;
				}
			}
			
			
			
			
			
			

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Use labels to display the images
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);

		JLabel lbText1 = new JLabel("Original image (Left)");
		lbText1.setHorizontalAlignment(SwingConstants.CENTER);
		JLabel lbText2 = new JLabel("Image after modification (Right)");
		lbText2.setHorizontalAlignment(SwingConstants.CENTER);
		lbIm1 = new JLabel(new ImageIcon(img));
		lbIm2 = new JLabel(new ImageIcon(imgmod));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		frame.getContentPane().add(lbText1, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 0;
		frame.getContentPane().add(lbText2, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		frame.getContentPane().add(lbIm1, c);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 1;
		frame.getContentPane().add(lbIm2, c);

		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		imageReader ren = new imageReader();
		ren.showIms(args);
	
	

	}

}