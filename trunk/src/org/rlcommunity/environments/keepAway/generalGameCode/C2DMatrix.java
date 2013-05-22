package org.rlcommunity.environments.keepAway.generalGameCode;

//Borrowed from http://www.public.iastate.edu/~mridul/cs572/Tunnel.zip 
public class C2DMatrix {
	
	S2DMatrix m_Matrix=null;

	public C2DMatrix(){
		m_Matrix=new S2DMatrix();
	}
	
	public void Identity(){
		m_Matrix.Identity();
	}
	  
	//create a rotation matrix
	  void Rotate(double rot)
	  {
		  S2DMatrix mat=new S2DMatrix();

	  	double Sin = Math.sin(rot);
	  	double Cos = Math.cos(rot);

	  	mat._11 = Cos;  mat._12 = Sin; mat._13 = 0;

	  	mat._21 = -Sin; mat._22 = Cos; mat._23 = 0;

	  	mat._31 = 0; mat._32 = 0;mat._33 = 1;
	    
//	  	System.out.println("Mat is: \n"+mat);

	    
	  	//and multiply
	    multiply(mat);
	    
//	    System.out.println("AFter rotate by:"+rot+" matrix is:");
//	    System.out.println(this);
	    		
	  }
	  
	//create a scale matrix
	  void Scale(double xScale, double yScale)
	  {
	  	S2DMatrix mat=new S2DMatrix();

	  	mat._11 = xScale; mat._12 = 0; mat._13 = 0;

	  	mat._21 = 0; mat._22 = yScale; mat._23 = 0;

	  	mat._31 = 0; mat._32 = 0; mat._33 = 1;

	  	//and multiply
	  	multiply(mat);
	  }

	private void multiply(S2DMatrix mIn) {
		S2DMatrix mat_temp=new S2DMatrix();
		//first row
		mat_temp._11 = (m_Matrix._11*mIn._11) + (m_Matrix._12*mIn._21) + (m_Matrix._13*mIn._31);
		mat_temp._12 = (m_Matrix._11*mIn._12) + (m_Matrix._12*mIn._22) + (m_Matrix._13*mIn._32);
		mat_temp._13 = (m_Matrix._11*mIn._13) + (m_Matrix._12*mIn._23) + (m_Matrix._13*mIn._33);

		//second
		mat_temp._21 = (m_Matrix._21*mIn._11) + (m_Matrix._22*mIn._21) + (m_Matrix._23*mIn._31);
		mat_temp._22 = (m_Matrix._21*mIn._12) + (m_Matrix._22*mIn._22) + (m_Matrix._23*mIn._32);
		mat_temp._23 = (m_Matrix._21*mIn._13) + (m_Matrix._22*mIn._23) + (m_Matrix._23*mIn._33);

		//third
		mat_temp._31 = (m_Matrix._31*mIn._11) + (m_Matrix._32*mIn._21) + (m_Matrix._33*mIn._31);
		mat_temp._32 = (m_Matrix._31*mIn._12) + (m_Matrix._32*mIn._22) + (m_Matrix._33*mIn._32);
		mat_temp._33 = (m_Matrix._31*mIn._13) + (m_Matrix._32*mIn._23) + (m_Matrix._33*mIn._33);

		m_Matrix = mat_temp;	
		
	}
	
	//create a transformation matrix
	void Translate(double x, double y)
	{
		S2DMatrix mat=new S2DMatrix();

	    mat._11 = 1; mat._12 = 0; mat._13 = 0;

	    mat._21 = 0; mat._22 = 1; mat._23 = 0;

	    mat._31 = x;    mat._32 = y;    mat._33 = 1;

	    multiply(mat);
	}
	
	//create a rotation matrix from a 2D vector
	void Rotate(Vector2D fwd,Vector2D side)
	{
		S2DMatrix mat=new S2DMatrix();

	    mat._11 = fwd.x;  mat._12 = fwd.y; mat._13 = 0;

	    mat._21 = side.x; mat._22 = side.y; mat._23 = 0;

	    mat._31 = 0; mat._32 = 0;mat._33 = 1;

	    multiply(mat);
	}

//	http://h1.ripway.com/MrAwesome/IsoRPGSource.zip 
	public void TransformVector2Ds(Vector2D vPoint) {
		        double tempX = (m_Matrix._11*vPoint.x) + (m_Matrix._21*vPoint.y) +
		                       (m_Matrix._31);

		        double tempY = (m_Matrix._12*vPoint.x) + (m_Matrix._22*vPoint.y) +
		                       (m_Matrix._32);

//		        System.out.println("Matrix: \n"+this+" turned: "+vPoint+" into "+tempX+","+tempY);

		        		vPoint.x = tempX;
		        vPoint.y = tempY;
		        
	}
	
	

	public String toString(){
		return m_Matrix.toString();
	}
}


class S2DMatrix{
	 double _11, _12, _13;
	  double _21, _22, _23;
	  double _31, _32, _33;
	  
	void Identity(){
		_11=1;
		_12=0;
		_13=0;
		_21=0;
		_22=1;
		_23=0;
		_31=0;
		_32=0;
		_33=1;
		}
	  
		public String toString(){
			String S=""+_11+"\t"+_12+"\t"+_13+"\n";
			S+=""+_21+"\t"+_22+"\t"+_23+"\n";
			S+=""+_31+"\t"+_32+"\t"+_33+"\n";
		return S;
		}
		
	  

}

