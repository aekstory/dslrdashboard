package com.dslr.dashboard;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PtpPartialObjectProccessor implements IPtpCommandFinalProcessor {
	
	private static String TAG = "GetPartialObjectProcessor";
	
	public interface PtpPartialObjectProgressListener {
		public void onProgress(int offset);
	}
	
	private PtpPartialObjectProgressListener mProgressListener;
	
	public void setProgressListener(PtpPartialObjectProgressListener listener){
		mProgressListener = listener;
	}
	private PtpObjectInfo mObjectInfo;
    private int mOffset = 0;
    private int mMaxSize = 0x100000;
	//private byte[] _objectData;
	private File mFile;
	private OutputStream mStream;
	
//	public byte[] pictureData(){
//		return _objectData;
//	}
	public PtpObjectInfo objectInfo(){
		return mObjectInfo;
	}
	public int maxSize(){
		return mMaxSize;
	}
	public PtpPartialObjectProccessor(PtpObjectInfo objectInfo, File file){
		this(objectInfo, 0x100000, file);
	}
	public PtpPartialObjectProccessor(PtpObjectInfo objectInfo, int maxSize, File file){
		mFile = file;
		mObjectInfo = objectInfo;
		mMaxSize = maxSize;
		//_objectData = new byte[_objectInfo.objectCompressedSize];
		
		try {
			mStream = new BufferedOutputStream(new FileOutputStream(mFile));
		} catch (FileNotFoundException e) {
		}
	}
	
	public boolean doFinalProcessing(PtpCommand cmd) {
        boolean result = false;
        
        int count = cmd.incomingData().getPacketLength() - 12;
        
        if (cmd.isResponseOk())
        {
            try {
				mStream.write(cmd.incomingData().data(), 12, count);
			} catch (IOException e) {
			}
            
        	//System.arraycopy(cmd.incomingData().data(), 12, _objectData, _offset, count);
            if ((mOffset + count) < mObjectInfo.objectCompressedSize)
            {
                mOffset += count;
                cmd.getParams().set(1, mOffset);
                if ((mOffset + mMaxSize) > mObjectInfo.objectCompressedSize)
                	cmd.getParams().set(2, mObjectInfo.objectCompressedSize - mOffset);
                else
                	cmd.getParams().set(2, mMaxSize);
                
                result = true;
            }
            else {
            	mOffset += count;
            	try {
					mStream.flush();
	            	mStream.close();
				} catch (IOException e) {
				}
            }
            if (mProgressListener != null){
            	mProgressListener.onProgress(mOffset);
            }
        }
        return result;
    }			 
}
