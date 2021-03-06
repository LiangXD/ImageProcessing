import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

public class ImgCompress {
	private String[][][] CompressMessage;//压缩后图片的编码
	private Tree tree;//huffman建立的完全二叉树
	private double CompressRate;//压缩比例
	private double SNR;//信噪比
	
	//Huffman编码压缩接口
	public void HuffmanComPress(int[] IniPixelsData,  int width, int height) {
		int[][][] input3DData = ProcessPixelData.processOneToThreeDeminsion(IniPixelsData,width,height);
		Map<Integer, Integer> statistics = Getstatistics(input3DData, width, height);
		List<Node> leafs = new ArrayList<Node>();
		tree = buildTree(statistics, leafs);
		Map<Integer, String> encodingChart = buildEncodingInfo(leafs);
		ComputeRate(statistics, encodingChart, width*height*3*8);
		SaveEncodingChart(encodingChart);
		CompressMessage = EncodingImg(input3DData, width, height, encodingChart);
		return;
	}
	
	//Huffman解码接口
	public int[] HuffmanDecode(int width, int height) {
		int[][][] input3DData = new int[height][width][4];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				for(int k = 0; k < 4; k++) {
					if(k == 0) {
						input3DData[i][j][k] = 255;
					}
					else {
						input3DData[i][j][k] = DecodeImg(CompressMessage[i][j][k - 1]);
					}
				}
			}
		}
		return ProcessPixelData.convertToOneDim(input3DData, width, height);
	}
	
	//统计像素分布特征
	private static Map<Integer, Integer> Getstatistics(int[][][] input3DData, int width, int height) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				for(int k = 1; k <= 3; k++) {
					int key = input3DData[i][j][k];
					if(map.containsKey(key)) {
						map.put(key, map.get(key)+1);
					}
					else {
						map.put(key, 1);
					}
				}
			}
		}

		return map;
	}
	
	//建立二叉树
	private static Tree buildTree(Map<Integer, Integer> statistics,
			List<Node> leafs) {
		Integer[] keys = statistics.keySet().toArray(new Integer[0]);

		PriorityQueue<Node> priorityQueue = new PriorityQueue<Node>();
		for (int i = 0 ; i < keys.length; i++)
		{
			Node node = new Node();
			node.setInteger(keys[i]);
			node.setFrequence(statistics.get(keys[i]));
			priorityQueue.add(node);
			leafs.add(node);
		}

		int size = priorityQueue.size();
		for (int i = 1; i <= size - 1; i++) {
			Node node1 = priorityQueue.poll();
			Node node2 = priorityQueue.poll();

			Node sumNode = new Node();
			sumNode.setInteger(256);//父节点的值无所谓随便赋一个值
			sumNode.setFrequence(node1.getFrequence() + node2.getFrequence());

			sumNode.setLeftNode(node1);
			sumNode.setRightNode(node2);

			node1.setParent(sumNode);
			node2.setParent(sumNode);

			priorityQueue.add(sumNode);
		}

		Tree tree = new Tree();
		tree.setRoot(priorityQueue.poll());
		return tree;
	}
	
	//建立编码信息
	private static Map<Integer, String> buildEncodingInfo(List<Node> leafNodes) {
		Map<Integer, String> codewords = new HashMap<Integer, String>();
		for (Node leafNode : leafNodes) {
			Integer integer = new Integer(leafNode.getInteger());
			String codeword = "";
			Node currentNode = leafNode;

			do {
				if (currentNode.isLeftChild()) {
					codeword = "0" + codeword;
				} else {
					codeword = "1" + codeword;
				}

				currentNode = currentNode.getParent();
			} while (currentNode.getParent() != null);

			codewords.put(integer, codeword);
		}

		return codewords;
	}
	
	//保存像素值对应编码的表格
	private void SaveEncodingChart(Map<Integer, String> encodingChart) {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File("."));
		int result = chooser.showSaveDialog(null);
		if(result == JFileChooser.APPROVE_OPTION) {
			String name = chooser.getSelectedFile().getPath();
			if(name.length() <= 4 ||!name.substring(name.length()-4).equals(new String(".txt"))) {
				name += ".txt";
			}
			File file = new File(name);
			try {
				FileWriter fWriter = new FileWriter(file);
				for(Map.Entry<Integer, String> entry : encodingChart.entrySet()) {
					fWriter.write(entry.getKey() + " " + entry.getValue() + "\n");
				}
				fWriter.close();
				//ImageIO.write(panel.getImage(), "png", file);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	//根据原图的像素值进行编码
	private String[][][] EncodingImg(int[][][] input3DData, int width, int height, Map<Integer, String> encodingChart) {
		String[][][] string = new String[height][width][3];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				for(int k = 1; k <= 3; k++) {
					string[i][j][k-1] = encodingChart.get(input3DData[i][j][k]);
				}
			}
		}
		return string;
	}
	
	//计算压缩率
	private void ComputeRate(Map<Integer, Integer> statistics, Map<Integer, String> encodingChart, int TotalByte) {
		int Codingbyte = 0;
		for(Integer Pixel : statistics.keySet()) {
			Codingbyte += statistics.get(Pixel) * encodingChart.get(Pixel).length();
		}
		CompressRate = (float)Codingbyte / (float)(TotalByte);
	}
	
	//解码图片，根据编码获取像素值
	private int DecodeImg(String Binarystr) {
		Node currentNode = tree.getRoot();
		for(int i = 0; i < Binarystr.length(); i++) {
			if(Binarystr.charAt(i) == '0') {
				currentNode = currentNode.getLeftNode();
			}
			else {
				currentNode = currentNode.getRightNode();
			}
		}
		return currentNode.getInteger();
	}
	
	//计算信噪比
	public void ComputeSNR(int[] IniPixelsData, int[] ProPixelsData, int width, int height) {
		int[][][] input3DData = ProcessPixelData.processOneToThreeDeminsion(IniPixelsData,width,height);
		int[][][] input3DData1 = ProcessPixelData.processOneToThreeDeminsion(ProPixelsData,width,height);
		double sum1 = 0;
		double sum2 = 0;
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				for(int k = 0; k <= 3; k++) {
					double t1 = (double)input3DData[i][j][k]/100.0;
					double t2 = (double)input3DData1[i][j][k]/100.0;
					sum1 += Math.pow(t1, 2);
					sum2 += Math.pow(t1-t2, 2);
				}
			}
		}
		SNR = 10 * Math.log10(sum1/sum2);
	}
	
	//计算PSNR
	public double ComputePSNR(int[] OriPic, int[] AnotherPic, int width, int height) {
		double[][] PiC1_Y = GETYChanel(OriPic,width,height);
		double[][] PIC2_Y = GETYChanel(AnotherPic, width, height);
		double MSE = ComputeMSE(PiC1_Y, PIC2_Y,width,height);
		double PSNR = ComputePSNR(MSE, width, height);
		return PSNR;
	}
	
	//计算SSIM
	public double ComputeSSIM(int[] OriPic, int[] AnotherPic, int width, int height) {
		SpaceProcess spaceProcess = new SpaceProcess();
		double[][] PiC1_Y = GETYChanel(OriPic,width,height);//获取Y通道
		double[][] PiC2_Y = GETYChanel(AnotherPic, width, height);//获取Y通道
		double c1 = Math.pow(0.01 * 255, 2);
		double c2 = Math.pow(0.03 * 255, 2);
		//通过高斯加权计算均值方差协方差
		double[][] ave1 = spaceProcess.GaussianBlur2(PiC1_Y, width, height, 11, 1.5);
		double[][] ave2 = spaceProcess.GaussianBlur2(PiC2_Y, width, height, 11, 1.5);
		double[][] ave12 = ComputeProduct(ave1, ave2, width, height);
		ave1 = ComputeProduct(ave1, ave1, width, height);
		ave2 = ComputeProduct(ave2, ave2, width, height);
		double[][] var1 = spaceProcess.GaussianBlur2(ComputeProduct(PiC1_Y, PiC1_Y, width, height), width, height, 11, 1.5);
		var1 = ComputeMinus(var1, ave1, width, height);
		double[][] var2 = spaceProcess.GaussianBlur2(ComputeProduct(PiC2_Y, PiC2_Y, width, height), width, height, 11, 1.5);
		var2 = ComputeMinus(var2, ave2, width, height);
		double[][] covar = spaceProcess.GaussianBlur2(ComputeProduct(PiC1_Y, PiC2_Y, width, height), width, height, 11, 1.5);
		covar = ComputeMinus(covar, ave12, width, height);
		
		double sum = 0.0;
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				sum += ((2*ave12[i][j]+c1)*(2*covar[i][j]+c2))/((ave1[i][j] + ave2[i][j] + c1) * (var1[i][j] + var2[i][j] + c2));
			}
		}
		//计算SSIM均值
		double SSIM = sum/(width*height*1.0);
		return SSIM;
	}
	
	//获取信噪比
	public double GetSNR() {
		return SNR;
	}
	
	//获取压缩率
	public double GetRate() {
		return CompressRate;
	}
	
	//RGB TO YCBCR,Then GET Y
	private double[][] GETYChanel(int[] IniPixelsData, int width, int height ) {
		int[][][] input3DData = ProcessPixelData.processOneToThreeDeminsion(IniPixelsData,width,height);
		double[][] Result = new double[height][width];
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				int R = input3DData[i][j][1];
				int G = input3DData[i][j][2];
				int B = input3DData[i][j][3];
				Result[i][j] = 0.257*R+0.564*G+0.098*B+16;
			}
		}
		
		return Result;
	}
	
	//计算均方差
	private double ComputeMSE(double[][] PiC1_Y, double[][] PIC2_Y, int width, int height) {
		double result = 0.0;
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				result += Math.pow((PiC1_Y[i][j] - PIC2_Y[i][j]), 2);
			}
		}
		result /= (height*width*1.0);
		return result;
	}
	
	//计算峰值信噪比
	private double ComputePSNR(double MSE, int width, int height) {
		double MAX1 = 255;
		double result = 0.0;
		result = 20 * Math.log10(MAX1/Math.sqrt(MSE));
		return result;
	}
	
	//计算矩阵乘积
	private double[][] ComputeProduct(double[][] Mat1, double[][] Mat2, int width, int height) {
		double [][] Result = new double[height][width];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				Result[i][j] = Mat1[i][j] * Mat2[i][j];
			}
		}
		return Result;
	}
	
	//计算矩阵差
	private double[][] ComputeMinus(double[][] Mat1, double[][] Mat2, int width, int height) {
		double [][] Result = new double[height][width];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				Result[i][j] = Mat1[i][j] - Mat2[i][j];
			}
		}
		return Result;
	}
	
	/*
	//计算均值
	private double ComputeAVE(double[][] data, int width, int height) {
		double result = 0.0;
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				result += data[i][j];
			}
		}
		result /= (width * height * 1.0);
		return result;
	}
	
	//计算方差
	private double ComputeVAR(double[][] data, int width, int height, double ave) {
		double result = 0.0;
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				result += Math.pow(data[i][j] - ave, 2);
			}
		}
		result /= (height * width - 1.0);
		return result;
	}
	
	//计算协方差
	private double ComputeCOVAR(double[][] data1, double[][] data2, double ave1, double ave2, int width, int height) {
		double result = 0.0;
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				result += (data1[i][j] - ave1)*(data2[i][j] - ave2);
			}
		}
		result /= (height * width - 1.0);
		return result;
	}
	*/
}
