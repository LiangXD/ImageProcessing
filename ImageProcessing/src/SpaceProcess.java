import java.util.Random;
import java.util.zip.Inflater;

import javax.swing.text.html.HTMLDocument.HTMLReader.PreAction;

public class SpaceProcess {
	private int[] IniPicCount = new int[256];
	private int[] ProcessCount = new int[256];
	private int[][] filter;
	private double[][] filter1;
	
	//灰度图直方图均衡化函数
	public int[] HistogramEQ(int[] IniPixelsData, int width, int height) {
		int[][][] input3DData = ProcessPixelData.processOneToThreeDeminsion(IniPixelsData,width,height);
		float[] ratio = new float[256];
		int[] s = new int[256];
		float sum = 0;
		IniPicCount = RecordCount(input3DData, width, height);
		
		for(int i = 0; i < 256; i++) {
			ratio[i] = 255 * ((float)IniPicCount[i]) / (width * height);
		}
		
		for(int i = 0; i < 256; i++) {
			sum += ratio[i];
			s[i] = (int)Math.ceil(sum);
		}
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				input3DData[i][j][1] = s[input3DData[i][j][1]];
				input3DData[i][j][2] = s[input3DData[i][j][2]];
				input3DData[i][j][3] = s[input3DData[i][j][3]];
			}
		}
		
		ProcessCount = RecordCount(input3DData, width, height);
		
		return ProcessPixelData.convertToOneDim(input3DData, width, height);
	}
	
	//统计像素值分布
	public int[] RecordCount(int[][][] input3DData, int width, int height) {
		int[] count = new int[256];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				count[input3DData[i][j][1]]++;
			}
		}
		return count;
	}
	
	//获得初始像素值分布
	public int[] GetIniPicCount() {
		return IniPicCount;
	}
	
	//获得均衡化后像素值分布
	public int[] GetProcessCount() {
		return ProcessCount;
	}
	
	//彩色直方图均衡化
	public int[] ColorHistogramEQ(int[] IniPixelsData, int width, int height) {
		int[][][] input3DData = ProcessPixelData.processOneToThreeDeminsion(IniPixelsData,width,height);
		
		for(int k = 1; k < 3; k++) {
			int[] count = new int[256];
			count = RecordColorCount(input3DData, width, height, k);
			float[] ratio = new float[256];
			int[] s = new int[256];
			float sum = 0;
			for(int i = 0; i < 256; i++) {
				ratio[i] = 255 * ((float)count[i]) / (width * height);
			}
			for(int i = 0; i < 256; i++) {
				sum += ratio[i];
				s[i] = (int)Math.ceil(sum);
			}
			for(int i = 0; i < height; i++) {
				for(int j = 0; j < width; j++) {
					input3DData[i][j][k] = s[input3DData[i][j][k]];
				}
			}
		}
				
		return ProcessPixelData.convertToOneDim(input3DData, width, height);
	}
	
	//获取彩色单通道的像素值分布
	private int[] RecordColorCount(int[][][] input3DData, int width, int height, int k) {
		int[] count = new int[256];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				count[input3DData[i][j][k]]++;
			}
		}
		return count;
	}
	
	//彩色均值直方图均衡化
	public int[] ColorAveHistogramEQ(int[] IniPixelsData, int width, int height) {
		int[][][] input3DData = ProcessPixelData.processOneToThreeDeminsion(IniPixelsData,width,height);
		int[] count = new int[256];
		count = RecordAveCount(input3DData, width, height);
		float[] ratio = new float[256];
		int[] s = new int[256];
		float sum = 0;
		for(int i = 0; i < 256; i++) {
			ratio[i] = 255 * ((float)count[i]) / (width * height);
		}
		for(int i = 0; i < 256; i++) {
			sum += ratio[i];
			s[i] = (int)Math.ceil(sum);
		}
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				input3DData[i][j][1] = s[input3DData[i][j][1]];
				input3DData[i][j][2] = s[input3DData[i][j][2]];
				input3DData[i][j][3] = s[input3DData[i][j][3]];
			}
		}
		return ProcessPixelData.convertToOneDim(input3DData, width, height);
	}
	
	//获取彩色3通道的像素值平均分布
	private int[] RecordAveCount(int[][][] input3DData, int width, int height) {
		int[] count = new int[256];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				count[input3DData[i][j][1]]++;
				count[input3DData[i][j][2]]++;
				count[input3DData[i][j][3]]++;
			}
		}
		
		for(int i = 0; i < 256; i++) {
			count[i] /= 3;
		}
		return count;
	}
	
	//强度直方图均衡化
	public int[] ItensityHistogramEQ(int[] IniPixelsData, int width, int height) {
		int[][][] input3DData = ProcessPixelData.processOneToThreeDeminsion(IniPixelsData,width,height);
		double[][] hue = new double[height][width];
		double[][] saturability = new double[height][width];
		double[][] itensity = new double[height][width];
		
		//获得色调
		hue = getHue(input3DData, width, height);
		//获得饱和度
		saturability = getSaturability(input3DData, width, height);
		//获得亮度
		itensity = getItensity(input3DData, width, height);
		
		int[][] ITENSITY = new int[height][width];
		int max = 0;
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				ITENSITY[i][j] = (int)itensity[i][j];
				max = max > ITENSITY[i][j] ? max: ITENSITY[i][j];
			}
		}
		int[] count = RecordICount(ITENSITY, height, width, max);
		float[] ratio = new float[max+1];
		float sum = 0;
		int[] s = new int[max+1];
		for(int i = 0; i <= max; i++) {
			ratio[i] = max * ((float)count[i]) / (width * height);
		}
		for(int i = 0; i <= max; i++) {
			sum += ratio[i];
			s[i] = (int)Math.ceil(sum);
		}
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				itensity[i][j] = s[ITENSITY[i][j]];
			}
		}
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				int R = 0, G = 0, B = 0;
				if(hue[i][j] >= 0 && hue[i][j] < 120) {
					B = (int)(itensity[i][j] * (1 - saturability[i][j]));
					R = (int)(itensity[i][j] * (1 + (saturability[i][j] * Math.cos(Math.toRadians(hue[i][j])))/Math.cos(Math.toRadians(60 - hue[i][j]))));
					G = (int)(3 * itensity[i][j] - (R + B));
				}
				else if (hue[i][j] >= 120 && hue[i][j] < 240) {
					hue[i][j] -= 120;
					R = (int)(itensity[i][j] * (1 - saturability[i][j]));
					G = (int)(itensity[i][j] * (1 + (saturability[i][j] * Math.cos(Math.toRadians(hue[i][j])))/Math.cos(Math.toRadians(60 - hue[i][j]))));
					B = (int)(3 * itensity[i][j] - (R + G));
				} else if (hue[i][j] >= 240 && hue[i][j] < 360) {
					hue[i][j] -= 240;
					G = (int)(itensity[i][j] * (1 - saturability[i][j]));
					B = (int)(itensity[i][j] * (1 + (saturability[i][j] * Math.cos(Math.toRadians(hue[i][j])))/Math.cos(Math.toRadians(60 - hue[i][j]))));
					R = (int)(3 * itensity[i][j] - (G + B));
				}
				input3DData[i][j][1] = getClip(R);
				input3DData[i][j][2] = getClip(G);
				input3DData[i][j][3] = getClip(B);
			}
		}
		return ProcessPixelData.convertToOneDim(input3DData, width, height);
	}
	
	//记录强度通道数据分布
	private int[] RecordICount(int[][] itensity, int height, int width, int len) {
		int[] count = new int[len+1];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				count[itensity[i][j]]++;
			}
		}
		return count;
	}
	
	//获取色调
	private double[][] getHue(int[][][] Data, int width, int height) {
		double[][] result = new double[height][width];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				int R = Data[i][j][1];
				int G = Data[i][j][2];
				int B = Data[i][j][3];
				double t1 = 0.5 * (2 * R - G - B);
				double t2 = Math.sqrt(Math.pow((R - G), 2) + (R - B) * (G - B));
				double angle =  Math.toDegrees(Math.acos(t1/t2));
				if(B <= G) {
					result[i][j] = angle;
				} else {
					result[i][j] = 360 - angle;
				}
			}
		}
		return result;
	}
	
	//获取饱和度
	private double[][] getSaturability(int[][][] Data, int width, int height) {
		double[][] result = new double[height][width];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				int R = Data[i][j][1];
				int G = Data[i][j][2];
				int B = Data[i][j][3];
				int min = R < G?R:G;
				min = min < B?min:B;
				double t1 = R+G+B;
				result[i][j] = 1 - 3.0 * min/(t1);
			}
		}
		return result;
	}
	
	//获取亮度
	private double[][] getItensity(int[][][] Data, int width, int height) {
		double[][] result = new double[height][width];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				int R = Data[i][j][1];
				int G = Data[i][j][2];
				int B = Data[i][j][3];
				double t1 = R+G+B;
				result[i][j] = t1/3.0;
			}
		}
		return result;
	}
	
	//均值滤波
	public int[]  filterEQ(int[] IniPixelsData, int width, int height, int n) {
		int[][][] input3DData = ProcessPixelData.processOneToThreeDeminsion(IniPixelsData,width,height);
		filter = new int[n][n];
		for(int i = 0; i < n; i++) {
			for(int j = 0; j < n; j++) {
				filter[i][j] = 1; 
			}
		}
		int[][] RedData = new int[height][width];
		int[][] GreenData = new int[height][width];
		int[][] BlueData = new int[height][width];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				RedData[i][j] = input3DData[i][j][1];
				GreenData[i][j] = input3DData[i][j][2];
				BlueData[i][j] = input3DData[i][j][3];
			}
		}
		
		int[][] EXRedData = ExtendMatrix(RedData, width, height, n);
		int[][] ExGreenData = ExtendMatrix(GreenData, width, height, n);
		int[][] ExBlueData = ExtendMatrix(BlueData, width, height, n);
		
		int[][] filterData = FilerData(EXRedData,width,height,n);
		int[][] filterData1 = FilerData(ExGreenData, width, height, n);
		int[][] filterData2 = FilerData(ExBlueData, width, height, n);
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				input3DData[i][j][1] = filterData[i][j] / (n * n);
				input3DData[i][j][2] = filterData1[i][j] / (n * n);
				input3DData[i][j][3] = filterData2[i][j] / (n * n);
			}
		}
		return ProcessPixelData.convertToOneDim(input3DData, width, height);
	}
	
	//调和滤波
	public int[] filterHM(int[] IniPixelsData, int width, int height, int n) {
		int[][][] input3DData = ProcessPixelData.processOneToThreeDeminsion(IniPixelsData,width,height);
		int[][] RedData = new int[height][width];
		int[][] GreenData = new int[height][width];
		int[][] BlueData = new int[height][width];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				RedData[i][j] = input3DData[i][j][1];
				GreenData[i][j] = input3DData[i][j][2];
				BlueData[i][j] = input3DData[i][j][3];
			}
		}
		
		int[][] EXRedData = ExtendMatrix(RedData, width, height, n);
		int[][] ExGreenData = ExtendMatrix(GreenData, width, height, n);
		int[][] ExBlueData = ExtendMatrix(BlueData, width, height, n);
		
		int[][] filterData = FilerData2(EXRedData,width,height,n);
		int[][] filterData1 = FilerData2(ExGreenData, width, height, n);
		int[][] filterData2 = FilerData2(ExBlueData, width, height, n);
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				input3DData[i][j][1] = getClip(filterData[i][j]);
				input3DData[i][j][2] = getClip(filterData1[i][j]);
				input3DData[i][j][3] = getClip(filterData2[i][j]);
			}
		}
		return ProcessPixelData.convertToOneDim(input3DData, width, height);
	}
	
	//逆谐波滤波
	public int[] filterCHM(int[] IniPixelsData, int width, int height, int n, Double Q) {
		int[][][] input3DData = ProcessPixelData.processOneToThreeDeminsion(IniPixelsData,width,height);
		int[][] RedData = new int[height][width];
		int[][] GreenData = new int[height][width];
		int[][] BlueData = new int[height][width];
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				RedData[i][j] = input3DData[i][j][1];
				GreenData[i][j] = input3DData[i][j][2];
				BlueData[i][j] = input3DData[i][j][3];
			}
		}
		
		int[][] EXRedData = ExtendMatrix(RedData, width, height, n);
		int[][] ExGreenData = ExtendMatrix(GreenData, width, height, n);
		int[][] ExBlueData = ExtendMatrix(BlueData, width, height, n);
		
		int[][] filterData = FilerData3(EXRedData, width, height, n, Q);
		int[][] filterData1 = FilerData3(ExGreenData, width, height, n, Q);
		int[][] filterData2 = FilerData3(ExBlueData, width, height, n, Q);
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				input3DData[i][j][1] = getClip(filterData[i][j]);
				input3DData[i][j][2] = getClip(filterData1[i][j]);
				input3DData[i][j][3] = getClip(filterData2[i][j]);
			}
		}
		return ProcessPixelData.convertToOneDim(input3DData, width, height);
	}
	
	//中值滤波
	public int[] filterMedF(int[] IniPixelsData, int width, int height, int n) {
		int[][][] input3DData = ProcessPixelData.processOneToThreeDeminsion(IniPixelsData,width,height);
		int[][] RedData = new int[height][width];
		int[][] GreenData = new int[height][width];
		int[][] BlueData = new int[height][width];
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				RedData[i][j] = input3DData[i][j][1];
				GreenData[i][j] = input3DData[i][j][2];
				BlueData[i][j] = input3DData[i][j][3];
			}
		}
		
		int[][] EXRedData = ExtendMatrix(RedData, width, height, n);
		int[][] ExGreenData = ExtendMatrix(GreenData, width, height, n);
		int[][] ExBlueData = ExtendMatrix(BlueData, width, height, n);
		
		int[][] filterData = FilerData4(EXRedData,width,height,n);
		int[][] filterData1 = FilerData4(ExGreenData, width, height, n);
		int[][] filterData2 = FilerData4(ExBlueData, width, height, n);
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				input3DData[i][j][1] = filterData[i][j];
				input3DData[i][j][2] = filterData1[i][j];
				input3DData[i][j][3] = filterData2[i][j];
			}
		}
		return ProcessPixelData.convertToOneDim(input3DData, width, height);
	}
	
	//几何均值滤波
	public int[] filterGMF(int[] IniPixelsData, int width, int height, int n) {
		int[][][] input3DData = ProcessPixelData.processOneToThreeDeminsion(IniPixelsData,width,height);
		int[][] RedData = new int[height][width];
		int[][] GreenData = new int[height][width];
		int[][] BlueData = new int[height][width];
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				RedData[i][j] = input3DData[i][j][1];
				GreenData[i][j] = input3DData[i][j][2];
				BlueData[i][j] = input3DData[i][j][3];
			}
		}
		
		int[][] EXRedData = ExtendMatrix(RedData, width, height, n);
		int[][] ExGreenData = ExtendMatrix(GreenData, width, height, n);
		int[][] ExBlueData = ExtendMatrix(BlueData, width, height, n);
		
		int[][] filterData = FilerData5(EXRedData,width,height,n);
		int[][] filterData1 = FilerData5(ExGreenData, width, height, n);
		int[][] filterData2 = FilerData5(ExBlueData, width, height, n);
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				input3DData[i][j][1] = filterData[i][j];
				input3DData[i][j][2] = filterData1[i][j];
				input3DData[i][j][3] = filterData2[i][j];
			}
		}
		return ProcessPixelData.convertToOneDim(input3DData, width, height);
	}
	
	//最大值滤波
	public int[] filterMax(int[] IniPixelsData, int width, int height, int n) {
		int[][][] input3DData = ProcessPixelData.processOneToThreeDeminsion(IniPixelsData,width,height);
		int[][] RedData = new int[height][width];
		int[][] GreenData = new int[height][width];
		int[][] BlueData = new int[height][width];
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				RedData[i][j] = input3DData[i][j][1];
				GreenData[i][j] = input3DData[i][j][2];
				BlueData[i][j] = input3DData[i][j][3];
			}
		}
		
		int[][] EXRedData = ExtendMatrix(RedData, width, height, n);
		int[][] ExGreenData = ExtendMatrix(GreenData, width, height, n);
		int[][] ExBlueData = ExtendMatrix(BlueData, width, height, n);
		
		int[][] filterData = FilerData6(EXRedData,width,height,n);
		int[][] filterData1 = FilerData6(ExGreenData, width, height, n);
		int[][] filterData2 = FilerData6(ExBlueData, width, height, n);
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				input3DData[i][j][1] = filterData[i][j];
				input3DData[i][j][2] = filterData1[i][j];
				input3DData[i][j][3] = filterData2[i][j];
			}
		}
		return ProcessPixelData.convertToOneDim(input3DData, width, height);
	}
	
	//最小值滤波
	public int[] filterMin(int[] IniPixelsData, int width, int height, int n) {
		int[][][] input3DData = ProcessPixelData.processOneToThreeDeminsion(IniPixelsData,width,height);
		int[][] RedData = new int[height][width];
		int[][] GreenData = new int[height][width];
		int[][] BlueData = new int[height][width];
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				RedData[i][j] = input3DData[i][j][1];
				GreenData[i][j] = input3DData[i][j][2];
				BlueData[i][j] = input3DData[i][j][3];
			}
		}
		
		int[][] EXRedData = ExtendMatrix(RedData, width, height, n);
		int[][] ExGreenData = ExtendMatrix(GreenData, width, height, n);
		int[][] ExBlueData = ExtendMatrix(BlueData, width, height, n);
		
		int[][] filterData = FilerData7(EXRedData,width,height,n);
		int[][] filterData1 = FilerData7(ExGreenData, width, height, n);
		int[][] filterData2 = FilerData7(ExBlueData, width, height, n);
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				input3DData[i][j][1] = filterData[i][j];
				input3DData[i][j][2] = filterData1[i][j];
				input3DData[i][j][3] = filterData2[i][j];
			}
		}
		return ProcessPixelData.convertToOneDim(input3DData, width, height);
	}
	
	//产生高斯噪声
	public int[] ProduceGSNoise(int[] IniPixelsData, int width, int height, double mean, double standardVariance) {
		int[][][] input3DData = ProcessPixelData.processOneToThreeDeminsion(IniPixelsData,width,height);
		for(int k = 1; k <= 3; k++) {
			Random random = new Random();
			int SrcMax = 0;
			int DestMax = 0;
			for(int i = 0; i < height; i++) {
				for(int j = 0; j < width; j++) {
					SrcMax = input3DData[i][j][k] > SrcMax?input3DData[i][j][k]:SrcMax;
					double gv = getGaussianValue(mean, standardVariance, random);
					input3DData[i][j][k] += gv;
					DestMax = input3DData[i][j][k] > DestMax?input3DData[i][j][k]:DestMax;
				}
			}
			
			double rate = (double)SrcMax/(double)DestMax;
			for(int i = 0; i < height; i++) {
				for(int j = 0; j < width; j++) {
					double temp = input3DData[i][j][k]*rate;
					input3DData[i][j][k] = (int)temp;
				}
			}
		}
		return ProcessPixelData.convertToOneDim(input3DData, width, height);
	}
	
	//产生椒盐噪声
	public int[] ProduceImpluseNoise(int[] IniPixelsData, int width, int height, double PRatio, double SRatio) {
		int[][][] input3DData = ProcessPixelData.processOneToThreeDeminsion(IniPixelsData,width,height);
		int TotalNum = width * height;
		int saltNum = (int)(TotalNum * SRatio);
		int peperNum = (int)(TotalNum * PRatio);
		for(int i = 0; i < saltNum; i++) {
			int t1 = (int) (Math.random() * height);
			int t2 = (int) (Math.random() * width);
			for(int k = 1; k <= 3; k++) {
				input3DData[t1][t2][k] = 255;
			}
		}
		for(int i = 0; i < peperNum; i++) {
			int t1 = (int) (Math.random() * height);
			int t2 = (int) (Math.random() * width);
			for(int k = 1; k <= 3; k++) {
				input3DData[t1][t2][k] = 0;
			}
		}
		return ProcessPixelData.convertToOneDim(input3DData, width, height);
	}
	
	//获取高斯值
	private double getGaussianValue(
		      double mean, double standardVariance, Random random) {
		    
		    double result = random.nextGaussian() * standardVariance + mean;
		    
		    return result;
		  }
	
	//滤波函数（滤波器int形）
	private int[][] FilerData(int[][] eXRedData, int width, int height, int n) {
		// TODO Auto-generated method stub
		int len = n -1;
		int[][] ProRedData = new int[height + len * 2][width + len * 2];
		for(int heightpos  = 0; heightpos <  height + n - 1; heightpos ++) {
			for(int widthpos = 0; widthpos < width + n - 1; widthpos ++) {
				
				int sum = 0;
				for(int i = heightpos; i < heightpos + n; i++) {
					for(int j = widthpos; j < widthpos + n; j++) {
						sum += eXRedData[i][j] * filter[i-heightpos][j-widthpos];
					}
				}
				ProRedData[heightpos + n/2][widthpos + n/2] = sum;
			}
		}
		
		int[][] filterdata = new int[height][width];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j <width; j++) {
				filterdata[i][j] = ProRedData[i + n - 1][j + n - 1];
			}
		}
		
		return filterdata;
	}
	
	//滤波函数（滤波器double形）
	private int[][] FilerData1(int[][] eXRedData, int width, int height, int n) {
		// TODO Auto-generated method stub
		int len = n -1;
		int[][] ProRedData = new int[height + len * 2][width + len * 2];
		for(int heightpos  = 0; heightpos <  height + n - 1; heightpos ++) {
			for(int widthpos = 0; widthpos < width + n - 1; widthpos ++) {
				
				double sum = 0;
				for(int i = heightpos; i < heightpos + n; i++) {
					for(int j = widthpos; j < widthpos + n; j++) {
						sum += eXRedData[i][j] * filter1[i-heightpos][j-widthpos];
					}
				}
				ProRedData[heightpos + n/2][widthpos + n/2] = (int)sum;
			}
		}
		
		int[][] filterdata = new int[height][width];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j <width; j++) {
				filterdata[i][j] = ProRedData[i + n - 1][j + n - 1];
			}
		}
		
		return filterdata;
	}

	//调和滤波
	private int[][] FilerData2(int[][] eXRedData, int width, int height, int n) {
		int len = n -1;
		int[][] ProRedData = new int[height + len * 2][width + len * 2];
		for(int heightpos  = 0; heightpos <  height + n - 1; heightpos ++) {
			for(int widthpos = 0; widthpos < width + n - 1; widthpos ++) {
				
				double sum = 0;
				for(int i = heightpos; i < heightpos + n; i++) {
					for(int j = widthpos; j < widthpos + n; j++) {
						if(eXRedData[i][j] != 0) {
							sum += 1.0 / eXRedData[i][j];
						}
					}
				}
				if(sum != 0) {
					ProRedData[heightpos + n/2][widthpos + n/2] = (int)(n*n*1.0/sum);
				} else {
					ProRedData[heightpos + n/2][widthpos + n/2] = 0;
				}
			}
		}
		
		int[][] filterdata = new int[height][width];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j <width; j++) {
				filterdata[i][j] = ProRedData[i + n - 1][j + n - 1];
			}
		}
		
		return filterdata;
	}
	
	//逆谐波滤波
	private int[][] FilerData3(int[][] eXRedData, int width, int height, int n, Double Q) {
		int len = n -1;
		int[][] ProRedData = new int[height + len * 2][width + len * 2];
		for(int heightpos  = 0; heightpos <  height + n - 1; heightpos ++) {
			for(int widthpos = 0; widthpos < width + n - 1; widthpos ++) {
				
				double sum1 = 0;
				double sum2 = 0;
				for(int i = heightpos; i < heightpos + n; i++) {
					for(int j = widthpos; j < widthpos + n; j++) {
						if(Q > 0) {
							sum1 += Math.pow(eXRedData[i][j], Q);
							sum2 += Math.pow(eXRedData[i][j], Q+1);
						}
						else if(Q < 0 && Q >= -1) {
							if(eXRedData[i][j] != 0) {
								sum1 += Math.pow(eXRedData[i][j], Q);
							}
							sum2 += Math.pow(eXRedData[i][j], Q+1);
						}
						else if(Q < -1) {
							if(eXRedData[i][j] != 0) {
								sum1 += Math.pow(eXRedData[i][j], Q);
								sum2 += Math.pow(eXRedData[i][j], Q+1);
							}
						}
					}
				}
				ProRedData[heightpos + n/2][widthpos + n/2] = (int)(sum2/sum1);
			}
		}
		
		int[][] filterdata = new int[height][width];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j <width; j++) {
				filterdata[i][j] = ProRedData[i + n - 1][j + n - 1];
			}
		}
		
		return filterdata;
	}
	
	//中值滤波
	private int[][] FilerData4(int[][] eXRedData, int width, int height, int n) {
		int len = n -1;
		int[][] ProRedData = new int[height + len * 2][width + len * 2];
		for(int heightpos  = 0; heightpos <  height + n - 1; heightpos ++) {
			for(int widthpos = 0; widthpos < width + n - 1; widthpos ++) {
				
				int[] array = new int[n*n];
				for(int i = heightpos; i < heightpos + n; i++) {
					for(int j = widthpos; j < widthpos + n; j++) {
						array[(i-heightpos) * n + (j-widthpos)] = eXRedData[i][j];
					}
				}
				ProRedData[heightpos + n/2][widthpos + n/2] = GetMedian(array, n*n);
			}
		}
		
		int[][] filterdata = new int[height][width];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j <width; j++) {
				filterdata[i][j] = ProRedData[i + n - 1][j + n - 1];
			}
		}
		
		return filterdata;
	}
	
	//几何均值滤波
	private int[][] FilerData5(int[][] eXRedData, int width, int height, int n) {
		int len = n -1;
		int[][] ProRedData = new int[height + len * 2][width + len * 2];
		for(int heightpos  = 0; heightpos <  height + n - 1; heightpos ++) {
			for(int widthpos = 0; widthpos < width + n - 1; widthpos ++) {
				
				double Product = 1;
				for(int i = heightpos; i < heightpos + n; i++) {
					for(int j = widthpos; j < widthpos + n; j++) {
						if(eXRedData[i][j] != 0) {
							Product *= Math.pow((double)eXRedData[i][j], 1.0/(double)(n * n));
						}
					}
				}
				ProRedData[heightpos + n/2][widthpos + n/2] = (int)Product;
			}
		}
		
		int[][] filterdata = new int[height][width];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j <width; j++) {
				filterdata[i][j] = ProRedData[i + n - 1][j + n - 1];
			}
		}
		
		return filterdata;
	}
	
	//最大值滤波
	private int[][] FilerData6(int[][] eXRedData, int width, int height, int n) {
		int len = n -1;
		int[][] ProRedData = new int[height + len * 2][width + len * 2];
		for(int heightpos  = 0; heightpos <  height + n - 1; heightpos ++) {
			for(int widthpos = 0; widthpos < width + n - 1; widthpos ++) {
				
				int[] array = new int[n*n];
				for(int i = heightpos; i < heightpos + n; i++) {
					for(int j = widthpos; j < widthpos + n; j++) {
						array[(i-heightpos) * n + (j-widthpos)] = eXRedData[i][j];
					}
				}
				ProRedData[heightpos + n/2][widthpos + n/2] = GetMax(array, n*n);
			}
		}
		
		int[][] filterdata = new int[height][width];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j <width; j++) {
				filterdata[i][j] = ProRedData[i + n - 1][j + n - 1];
			}
		}
		
		return filterdata;
	}
	
	//最小值滤波
	private int[][] FilerData7(int[][] eXRedData, int width, int height, int n) {
		int len = n -1;
		int[][] ProRedData = new int[height + len * 2][width + len * 2];
		for(int heightpos  = 0; heightpos <  height + n - 1; heightpos ++) {
			for(int widthpos = 0; widthpos < width + n - 1; widthpos ++) {
				
				int[] array = new int[n*n];
				for(int i = heightpos; i < heightpos + n; i++) {
					for(int j = widthpos; j < widthpos + n; j++) {
						array[(i-heightpos) * n + (j-widthpos)] = eXRedData[i][j];
					}
				}
				ProRedData[heightpos + n/2][widthpos + n/2] = GetMin(array, n*n);
			}
		}
		
		int[][] filterdata = new int[height][width];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j <width; j++) {
				filterdata[i][j] = ProRedData[i + n - 1][j + n - 1];
			}
		}
		
		return filterdata;
	}
	
	//滤波函数（滤波器double形,数据double形）
	private double[][] FilerData8(double[][] eXRedData, int width, int height, int n) {
		int len = n -1;
		double[][] ProRedData = new double[height + len * 2][width + len * 2];
		for(int heightpos  = 0; heightpos <  height + n - 1; heightpos ++) {
			for(int widthpos = 0; widthpos < width + n - 1; widthpos ++) {
				
				double sum = 0;
				for(int i = heightpos; i < heightpos + n; i++) {
					for(int j = widthpos; j < widthpos + n; j++) {
						sum += eXRedData[i][j] * filter1[i-heightpos][j-widthpos];
					}
				}
				ProRedData[heightpos + n/2][widthpos + n/2] = sum;
			}
		}
		
		double[][] filterdata = new double[height][width];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j <width; j++) {
				filterdata[i][j] = ProRedData[i + n - 1][j + n - 1];
			}
		}
		
		return filterdata;
	}
	
	//冒泡排序
	private int GetMedian(int[] array, int len) {
		for(int i = 0; i < len; i++) {
			for(int j = i + 1; j < len; j++) {
				if(array[i] > array[j]) {
					int temp = array[i];
					array[i] = array[j];
					array[j] = temp;
				}
			}
		}
		return array[len/2];
	}
	
	//获取数组最大值
	private int GetMax(int[] array, int len) {
		int max = 0;
		for(int i = 0; i < len; i++) {
			if(array[i] > max) {
				max = array[i];
			}
		}
		return max;
	}
	
	//获取数组最小值
	private int GetMin(int[] array, int len) {
		int min = 9999;
		for(int i = 0; i < len; i++) {
			if(array[i] < min) {
				min = array[i];
			}
		}
		return min;
	}
	
	//矩阵扩展，用于滤波,int数据
	private int[][] ExtendMatrix(int[][] Data, int width, int height, int n) {
		int len = n -1;
		int[][] ExMatrix = new int[height + len * 2][width + len * 2];
		for(int i = len; i < height + len; i++) {
			for(int j = len; j < width + len; j++) {
				ExMatrix[i][j] = Data[i - len][j - len];
			}
		}
		return ExMatrix;
	}
	
	//矩阵扩展，用于滤波,double数据
	private double[][] ExtendMatrix(double[][] Data, int width, int height, int n) {
		int len = n -1;
		double[][] ExMatrix = new double[height + len * 2][width + len * 2];
		for(int i = len; i < height + len; i++) {
			for(int j = len; j < width + len; j++) {
				ExMatrix[i][j] = Data[i - len][j - len];
			}
		}
		return ExMatrix;
	}

	//限制像素值范围
	private int getClip(int x) {
		return x > 255 ? 255 : x < 0 ? 0 : x;
	}
	
	//拉普拉斯滤波
	public int[] filterLaplacian(int[] IniPixelsData, int width, int height) {
		int[][][] input3DData = ProcessPixelData.processOneToThreeDeminsion(IniPixelsData,width,height);
		filter = new int[3][3];
		
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				filter[i][j] = 1;
			}
		}
		filter[1][1] = -8;
		int[][] RedData = new int[height][width];
		int[][] GreenData = new int[height][width];
		int[][] BlueData = new int[height][width];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				RedData[i][j] = input3DData[i][j][1];
				GreenData[i][j] = input3DData[i][j][2];
				BlueData[i][j] = input3DData[i][j][3];
			}
		}
		
		int[][] EXRedData = ExtendMatrix(RedData, width, height, 3);
		int[][] ExGreenData = ExtendMatrix(GreenData, width, height, 3);
		int[][] ExBlueData = ExtendMatrix(BlueData, width, height, 3);
		
		int[][] filterData = FilerData(EXRedData,width,height,3);
		int[][] filterData1 = FilerData(ExGreenData, width, height, 3);
		int[][] filterData2 = FilerData(ExBlueData, width, height, 3);
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				input3DData[i][j][1] -= filterData[i][j];
				input3DData[i][j][1] = getClip(input3DData[i][j][1]);
				input3DData[i][j][2] -= filterData1[i][j];
				input3DData[i][j][2] = getClip(input3DData[i][j][2]);
				input3DData[i][j][3] -= filterData2[i][j];
				input3DData[i][j][3] = getClip(input3DData[i][j][3]);
			}
		}
		return ProcessPixelData.convertToOneDim(input3DData, width, height);
	}
	
	//高提升滤波
	public int[] highBoostfiltering(int[] IniPixelsData, int width, int height, int n) {
		int[] fuzzy = filterEQ(IniPixelsData, width, height, n);
		int[][][] input3DData = ProcessPixelData.processOneToThreeDeminsion(IniPixelsData,width,height);
		int[][][] input3DData1 = ProcessPixelData.processOneToThreeDeminsion(fuzzy,width,height);
		int[][][] input3DData2 = new int[height][width][4];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				input3DData2[i][j][0] = input3DData[i][j][0];
				input3DData2[i][j][1] = input3DData[i][j][1] - input3DData1[i][j][1];
				input3DData2[i][j][2] = input3DData[i][j][2] - input3DData1[i][j][2];
				input3DData2[i][j][3] = input3DData[i][j][3] - input3DData1[i][j][3];
			}
		}
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				input3DData[i][j][1] += 3 * input3DData2[i][j][1];
				input3DData[i][j][1] = getClip(input3DData[i][j][1]);
				input3DData[i][j][2] += 3 * input3DData2[i][j][2];
				input3DData[i][j][2] = getClip(input3DData[i][j][2]);
				input3DData[i][j][3] += 3 * input3DData2[i][j][3];
				input3DData[i][j][3] = getClip(input3DData[i][j][3]);
			}
		}
		
		return ProcessPixelData.convertToOneDim(input3DData, width, height);
	}
	
	//水平边缘检测
	public int[] HorizonDetect(int[] IniPixelsData, int width, int height) {
		int[][][] input3DData = ProcessPixelData.processOneToThreeDeminsion(IniPixelsData,width,height);
		int[][] Xfilter = {{-1,-2,-1},{0,0,0},{1,2,1}};
		
		filter = Xfilter;
		
		int[][] RedData = new int[height][width];
		int[][] GreenData = new int[height][width];
		int[][] BlueData = new int[height][width];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				RedData[i][j] = input3DData[i][j][1];
				GreenData[i][j] = input3DData[i][j][2];
				BlueData[i][j] = input3DData[i][j][3];
			}
		}
		
		int[][] EXRedData = ExtendMatrix(RedData, width, height, 3);
		int[][] ExGreenData = ExtendMatrix(GreenData, width, height, 3);
		int[][] ExBlueData = ExtendMatrix(BlueData, width, height, 3);
		
		int[][] filterData = FilerData(EXRedData,width,height,3);
		int[][] filterData1 = FilerData(ExGreenData, width, height, 3);
		int[][] filterData2 = FilerData(ExBlueData, width, height, 3);
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				input3DData[i][j][1] = filterData[i][j];
				input3DData[i][j][1] = getClip(input3DData[i][j][1]);
				input3DData[i][j][2] = filterData1[i][j];
				input3DData[i][j][2] = getClip(input3DData[i][j][2]);
				input3DData[i][j][3] = filterData2[i][j];
				input3DData[i][j][3] = getClip(input3DData[i][j][3]);
			}
		}
		
		return ProcessPixelData.convertToOneDim(input3DData, width, height);
	}
	
	//垂直边缘检测
	public int[] VerticalDetect(int[] IniPixelsData, int width, int height) {
		int[][][] input3DData = ProcessPixelData.processOneToThreeDeminsion(IniPixelsData,width,height);
		int[][] Xfilter = {{-1,0,1},{-2,0,2},{-1,0,1}};
		
		filter = Xfilter;
		
		int[][] RedData = new int[height][width];
		int[][] GreenData = new int[height][width];
		int[][] BlueData = new int[height][width];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				RedData[i][j] = input3DData[i][j][1];
				GreenData[i][j] = input3DData[i][j][2];
				BlueData[i][j] = input3DData[i][j][3];
			}
		}
		
		int[][] EXRedData = ExtendMatrix(RedData, width, height, 3);
		int[][] ExGreenData = ExtendMatrix(GreenData, width, height, 3);
		int[][] ExBlueData = ExtendMatrix(BlueData, width, height, 3);
		
		int[][] filterData = FilerData(EXRedData,width,height,3);
		int[][] filterData1 = FilerData(ExGreenData, width, height, 3);
		int[][] filterData2 = FilerData(ExBlueData, width, height, 3);
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				input3DData[i][j][1] = filterData[i][j];
				input3DData[i][j][1] = getClip(input3DData[i][j][1]);
				input3DData[i][j][2] = filterData1[i][j];
				input3DData[i][j][2] = getClip(input3DData[i][j][2]);
				input3DData[i][j][3] = filterData2[i][j];
				input3DData[i][j][3] = getClip(input3DData[i][j][3]);
			}
		}
		
		return ProcessPixelData.convertToOneDim(input3DData, width, height);
	}
	
	//灰色色彩抖动1
	public int[] GreyDithering(int[] IniPixelsData, int width, int height) {
		int dither = 70;
		int[][][] input3DData = ProcessPixelData.processOneToThreeDeminsion(IniPixelsData,width,height);
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				Random random = new Random();
				int x = random.nextInt(dither * 2) - dither;
				input3DData[i][j][1] = (x + input3DData[i][j][1] > 128? 255:0);
				input3DData[i][j][2] = (x + input3DData[i][j][2] > 128? 255:0);
				input3DData[i][j][3] = (x + input3DData[i][j][3] > 128? 255:0);
			}
		}
		return ProcessPixelData.convertToOneDim(input3DData, width, height);
	}

	//Floyd–Steinberg dithering
	public int[] FSDithering(int[] IniPixelsData, int width, int height, int level) {
		int gap = 255 / (level - 1);
		int[][][] input3DData = ProcessPixelData.processOneToThreeDeminsion(IniPixelsData,width,height);
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				for(int k = 1; k < 4; k++) {
					int newData = Calculate(input3DData[i][j][k], gap);
					int error;
					error = input3DData[i][j][k] - newData;
					input3DData[i][j][k] = newData;
					if(j != width - 1) {
						int E1 = (input3DData[i][j+1][k] + (int)(error * 7.0 / 16));
						input3DData[i][j+1][k] = E1;
						if(i != height - 1) {
							int E4 = (input3DData[i+1][j+1][k] + (int)(error * 1.0 / 16));
							input3DData[i+1][j+1][k] = E4;
						}
					}
					if(i != height - 1) {
						int E3 = (input3DData[i+1][j][k] + (int)(error * 5.0 / 16));
						input3DData[i+1][j][k] = E3;
						if(j != 0) {
							int E2 = (input3DData[i+1][j-1][k] + (int)(error * 3.0 / 16));
							input3DData[i+1][j-1][k] = E2;
						}
					}
				}
			}
		}
		return ProcessPixelData.convertToOneDim(input3DData, width, height);
	}
	
	//量化分层
	private int Calculate(int data, int gap) {
		int Pre = data;
		data = (data/gap);
		int gravity = Pre%gap;
		int divide = gap / 2;
		if(gravity > divide) {
			data += 1;
		}
		data *= gap;
		return data;
	}
	
	//bayer dithering
	public int[] bayerDithering(int[] IniPixelsData, int width, int height, int level) {
		int[][][] input3DData = ProcessPixelData.processOneToThreeDeminsion(IniPixelsData,width,height);
		int[][] BayerPattern = {{0, 32, 8, 40, 2, 34, 10, 42},
				{48, 16, 56, 24, 50, 18, 58, 26,},
				{12, 44, 4, 36, 14, 46, 6, 38},
				{60, 28, 52, 20, 62, 30, 54, 22},
				{3, 35, 11, 43, 1, 33, 9, 41},
				{51, 19, 59, 27, 49, 17, 57, 25},
				{15, 47, 7, 39, 13, 45, 5, 37},
				{63, 31, 55, 23, 61, 29, 53, 21}};
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				for(int k = 1; k < 4; k++) {
					int temp = input3DData[i][j][k]>>2;
					if(temp > BayerPattern[i&7][j&7]) {
						input3DData[i][j][k] = 255;
					}
					else {
						input3DData[i][j][k] = 0;
					}
				}
			}
		}
		return ProcessPixelData.convertToOneDim(input3DData, width, height);
	}
	
	//高斯模糊
	public int[] GaussianBlur(int[] IniPixelsData, int width, int height, int len,Double radius) {
		int[][][] input3DData = ProcessPixelData.processOneToThreeDeminsion(IniPixelsData,width,height);
		double[][] matrix = GenerateGSMatrix(len,radius);
		
		filter1 = matrix;
		
		int[][] RedData = new int[height][width];
		int[][] GreenData = new int[height][width];
		int[][] BlueData = new int[height][width];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				RedData[i][j] = input3DData[i][j][1];
				GreenData[i][j] = input3DData[i][j][2];
				BlueData[i][j] = input3DData[i][j][3];
			}
		}
		
		int[][] EXRedData = ExtendMatrix(RedData, width, height, len);
		int[][] ExGreenData = ExtendMatrix(GreenData, width, height, len);
		int[][] ExBlueData = ExtendMatrix(BlueData, width, height, len);
		
		int[][] filterData = FilerData1(EXRedData,width,height,len);
		int[][] filterData1 = FilerData1(ExGreenData, width, height, len);
		int[][] filterData2 = FilerData1(ExBlueData, width, height, len);
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				input3DData[i][j][1] = filterData[i][j];
				input3DData[i][j][2] = filterData1[i][j];
				input3DData[i][j][3] = filterData2[i][j];
			}
		}
		
		return ProcessPixelData.convertToOneDim(input3DData, width, height);
	}
	
	public double[][] GaussianBlur2(double[][] IniPixelsData, int width, int height, int len,Double radius) {
		double[][] matrix = GenerateGSMatrix(len,radius);
		
		filter1 = matrix;
		
		double[][] EXRedData = ExtendMatrix(IniPixelsData, width, height, len);
		
		double[][] filterData = FilerData8(EXRedData,width,height,len);
		
		return filterData;
	}
	
	//产生高斯矩阵
	public double[][] GenerateGSMatrix(int len, Double var) {
		double[][] matrix = new double[len][len];
		double sum = 0.0;
		double A = 1 / (2 * Math.PI * var * var);
		for(int i = 0; i < len; i++) {
			int x = i - len/2;
			for(int j = 0; j < len; j++) {
				int y = j - len/2;
				matrix[i][j] = A * Math.pow(Math.E, (-1)*(x*x+y*y)/(2*var*var));
				sum += matrix[i][j];
			}
		}
		for(int i = 0; i < len; i++) {
			for(int j = 0; j < len; j++) {
				matrix[i][j] /= sum;
			}
		}
		return matrix;
	}
	
	//Canny检测
	public int[] CannyDetect(int[] IniPixelsData, int width, int height) {
		int[][][] input3DData = ProcessPixelData.processOneToThreeDeminsion(IniPixelsData,width,height);
		int[] ProcessData1 = GaussianBlur(IniPixelsData, width, height, 3, 2.0);
		
		int[][][] GX = GetGX(ProcessData1, width, height);
		int[][][] GY = GetGY(ProcessData1, width, height);
		
		int[][][] input3DData4 = new int[height][width][4];
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				input3DData4[i][j][0] = GX[i][j][0];
				for(int k = 1; k <= 3; k++) {
					input3DData4[i][j][k] = Math.abs(GX[i][j][k])+ Math.abs(GY[i][j][k]);
					input3DData4[i][j][k] = getClip(input3DData4[i][j][k]);
				}
			}
		}
		
		int[][][] input3DData2 = input3DData4;
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				for(int k = 1; k <= 3; k++) {
					double angle = Math.toDegrees(Math.atan2(GY[i][j][k],GX[i][j][k]));
					angle = Math.abs(angle);
					if(angle < 22.5) {
						if(j >= 1) {
							if(input3DData4[i][j][k] < input3DData4[i][j-1][k]) {
								input3DData2[i][j][k] = 0;
							}
						}
						if(j < width - 1) {
							if(input3DData4[i][j][k] < input3DData4[i][j+1][k]) {
								input3DData2[i][j][k] = 0;
							}
						}
					} else if (angle < 67.5){
						if(i >= 1 && j >= 1) {
							if(input3DData4[i][j][k] < input3DData4[i-1][j-1][k]) {
								input3DData2[i][j][k] = 0;
							}
						}
						if(i < height - 1 && j < width - 1) {
							if(input3DData4[i][j][k] < input3DData4[i+1][j+1][k]) {
								input3DData2[i][j][k] = 0;
							}
						}
					} else if (angle < 112.5){
						if(i >= 1) {
							if(input3DData4[i][j][k] < input3DData4[i-1][j][k]) {
								input3DData2[i][j][k] = 0;
							}
						}
						if(i < height - 1) {
							if(input3DData4[i][j][k] < input3DData4[i+1][j][k]) {
								input3DData2[i][j][k] = 0;
							}
						}
					} else if (angle < 157.5){
						if(i >= 1 && j < width - 1) {
							if(input3DData4[i][j][k] < input3DData4[i-1][j+1][k]) {
								input3DData2[i][j][k] = 0;
							}
						}
						if(i < height - 1 && j >= 1) {
							if(input3DData4[i][j][k] < input3DData4[i+1][j-1][k]) {
								input3DData2[i][j][k] = 0;
							}
						}
					} else {
						if(j >= 1) {
							if(input3DData4[i][j][k] < input3DData4[i][j-1][k]) {
								input3DData2[i][j][k] = 0;
							}
						}
						if(j < width - 1) {
							if(input3DData4[i][j][k] < input3DData4[i][j+1][k]) {
								input3DData2[i][j][k] = 0;
							}
						}
					}
				}
			}
		}
		
		
 		return ProcessPixelData.convertToOneDim(input3DData2, width, height);
	}
	
	//对水平方向进行边缘提取
	public int[][][] GetGX(int[] IniPixelsData, int width, int height) {
		int[][][] input3DData = ProcessPixelData.processOneToThreeDeminsion(IniPixelsData,width,height);
		int[][] Xfilter = {{-1,0,1},{-2,0,2},{-1,0,1}};
		
		filter = Xfilter;
		
		int[][] RedData = new int[height][width];
		int[][] GreenData = new int[height][width];
		int[][] BlueData = new int[height][width];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				RedData[i][j] = input3DData[i][j][1];
				GreenData[i][j] = input3DData[i][j][2];
				BlueData[i][j] = input3DData[i][j][3];
			}
		}
		
		int[][] EXRedData = ExtendMatrix(RedData, width, height, 3);
		int[][] ExGreenData = ExtendMatrix(GreenData, width, height, 3);
		int[][] ExBlueData = ExtendMatrix(BlueData, width, height, 3);
		
		int[][] filterData = FilerData(EXRedData,width,height,3);
		int[][] filterData1 = FilerData(ExGreenData, width, height, 3);
		int[][] filterData2 = FilerData(ExBlueData, width, height, 3);
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				input3DData[i][j][1] = filterData[i][j];
				input3DData[i][j][2] = filterData1[i][j];
				input3DData[i][j][3] = filterData2[i][j];
			}
		}
		
		return input3DData;
	}
	
	//对竖直方向进行边缘提取
	public int[][][] GetGY(int[] IniPixelsData, int width, int height) {
		int[][][] input3DData = ProcessPixelData.processOneToThreeDeminsion(IniPixelsData,width,height);
		int[][] Xfilter = {{-1,-2,-1},{0,0,0},{1,2,1}};
		
		filter = Xfilter;
		
		int[][] RedData = new int[height][width];
		int[][] GreenData = new int[height][width];
		int[][] BlueData = new int[height][width];
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				RedData[i][j] = input3DData[i][j][1];
				GreenData[i][j] = input3DData[i][j][2];
				BlueData[i][j] = input3DData[i][j][3];
			}
		}
		
		int[][] EXRedData = ExtendMatrix(RedData, width, height, 3);
		int[][] ExGreenData = ExtendMatrix(GreenData, width, height, 3);
		int[][] ExBlueData = ExtendMatrix(BlueData, width, height, 3);
		
		int[][] filterData = FilerData(EXRedData,width,height,3);
		int[][] filterData1 = FilerData(ExGreenData, width, height, 3);
		int[][] filterData2 = FilerData(ExBlueData, width, height, 3);
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				input3DData[i][j][1] = filterData[i][j];
				input3DData[i][j][2] = filterData1[i][j];
				input3DData[i][j][3] = filterData2[i][j];
			}
		}
		
		return input3DData;
	}
	
}
