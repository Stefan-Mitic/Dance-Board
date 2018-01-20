import javax.sound.sampled.*;
import java.io.*;
import java.util.Vector;
//import javazoom.jl.converter.*;

/**
 * The <code>SimpleSound</code> class is an implementation of the 
 * Java Sound API specifically designed for use with students.
 * http://java.sun.com/products/java-media/sound/index.html
 * <p>
 * This class allows for easy playback, and manipulation of AU, 
 * AIFF, and WAV files.
 * <p>
 *
 * Code & ideas for this class related to playing and 
 * viewing the sound were borrowed from the Java Sound Demo:
 * http://java.sun.com/products/java-media/sound/samples/JavaSoundDemo/
 *
 * Also, some code borrowed from Tritonus as noted.
 * 
 * Copyright Georgia Institute of Technology 2004
 * @author Ellie Harmon, ellie@cc.gatech.edu
 * @author Barbara Ericson ericson@mindspring.com
 */
public class SimpleSound{
  
  ///////////////////////////// fields ////////////////////////
  private static final int SAMPLE_RATE = 22050;
  private static final int NUM_BITS_PER_SAMPLE = 16;
  protected byte[] buffer; //An array of bytes representing the sound
  private AudioFileFormat audioFileFormat = null; //Contains information about this sound such as its length, format, and type
  private Vector playbacks = new Vector(); //A collection of the threads that are playing this sound
  private String fileName = null;
  
  ////////////////////////// constructor /////////////////////
  /**
   * Constructs a new SimpleSound from the given file.
   * @param fileName The File from which to create this sound.
   * @see SimpleSound#loadFromFile(String filename)
   */
  public SimpleSound(String fileName) {
      // load the sound from the file
      loadFromFile(fileName);
  }
  
  ///////////////////////// accessors ///////////////////////////
  /**
   * Method that returns the AudioFileFormat describing this 
   * simple sound.
   * @return the AudioFileFormat describing this sound
   * @see AudioFileFormat
   */
  public AudioFileFormat getAudioFileFormat() {
    return audioFileFormat;
  }
    
  /**
   * Method that returns the name of the file this sound came from.  
   * If this sound did not originate with a file, this value will 
   * be null.
   * @return the file name associated with this sound or null
   * @see #loadFromFile(String fileName)
   */
  public String getFileName() {
    return fileName;
  }
    
  
  /**************************************************************************/
  /********************** LOADING THE SOUND FROM FILE ***********************/
  /**************************************************************************/
  
  /**
   * Resets the fields of this sound so that it now represents the 
   * sound in the specified file.  If successful, the fileName 
   * ariable is updated such that it is equivalent to 
   * <code>inFileName</code>.
   *
   * @param inFileName the path and filename of the sound we want to 
   *                   represent.
   * @throws SoundException if any problem is encountered while
   *                            reading in from the file.
   */
  public void loadFromFile(String inFileName) {
    // try to prevent a null pointer exception
    if(inFileName == null) {
      System.out.println("You must pass in a valid file name.  Please try again.");  
    }
    
    /* get the File object representing the file named inFileName 
     * and make sure it exists */
    File file = new File(inFileName);
    if(!file.exists()) {
      System.out.println("The file: " + inFileName + " doesn't exist");
    }
    
    // create an audioInputStream from this file
    
    AudioInputStream audioInputStream;
    try {
      audioInputStream = AudioSystem.getAudioInputStream(file);
    } catch(Exception e) {
      System.out.println("Unable to read from file " + 
                 inFileName + ".  The file type is unsupported.  " + 
                 "Are you sure you're using a WAV, AU, or " +
                 "AIFF file (some .wav files are encoded " +
                 "using mp3)?  Try using SimpleSound.convert(" +
                 "String oldName, String newName) and then " +
                 "try to read the new name.");
      return;
    }
    
    /* We need to make an array representing this sound, so the 
     * number of bytes we will be storing cannot be greater than 
     * Integer.MAX_VALUE.  The JavaSound API also supports only 
     * integer length frame lengths. 
     * (See AudioFileFormat.getFrameLength().  I don't know why 
     * this is inconsistent with AudioInputStream.getFrameLength().)
     */
    if((audioInputStream.getFrameLength() * audioInputStream.getFormat().getFrameSize()) > Integer.MAX_VALUE) {
      System.out.println("The sound in file: " + inFileName + " is too long. Try using a shorter sound.");
    }
    int bufferSize = (int)audioInputStream.getFrameLength() * audioInputStream.getFormat().getFrameSize();
    
    buffer = new byte[bufferSize];
    
    int numBytesRead = 0;
    int offset = 0;
    
    //read all the bytes into the buffer
    while(true) {
      try {
        numBytesRead = audioInputStream.read(buffer, offset, bufferSize);
        if(numBytesRead == -1)//no more data
          break;
        else
          offset += numBytesRead;
      } catch(Exception e) {
        System.out.println("Problems reading the input stream.  ");
      }
    }
    
    
    /* set the format of the file, assuming that the extension 
     * is correct
     */
    if(inFileName.toLowerCase().endsWith(".wav")) {
      audioFileFormat = new AudioFileFormat(AudioFileFormat.Type.WAVE, audioInputStream.getFormat(), (int)audioInputStream.getFrameLength());
    } else if(inFileName.toLowerCase().endsWith(".au")) {
      audioFileFormat = new AudioFileFormat(AudioFileFormat.Type.AU, audioInputStream.getFormat(), (int)audioInputStream.getFrameLength());
    } else if (inFileName.toLowerCase().endsWith(".aif") || inFileName.toLowerCase().endsWith(".aiff")) {
      audioFileFormat = new AudioFileFormat(AudioFileFormat.Type.AIFF, audioInputStream.getFormat(), (int)audioInputStream.getFrameLength());
    } else {
      System.out.println("Unsupported file type.  Please try again with a file that ends in .wav, .au, .aif, or .aiff");
    }
    
    this.fileName = inFileName; 
  }
  
  /**************************************************************************/
  /************************** PLAYING THE SOUND ****************************/
  /**************************************************************************/
  
  /**
   * Creates an <code>AudioInputStream</code> for this sound from the 
   * <code>buffer</code> and the <code>audioFileFormat</code>.
   * @return an AudioInputStream representing this sound.
   * @see AudioInputStream
   */
  public AudioInputStream makeAIS() {
    AudioFileFormat.Type fileType = audioFileFormat.getType();
    ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
    int frameSize = audioFileFormat.getFormat().getFrameSize();
    
    AudioInputStream audioInputStream = new AudioInputStream(bais, audioFileFormat.getFormat(), buffer.length/frameSize);
    return audioInputStream;
  }
  
  /**
   * Creates a new Playback thread and starts it.   The thread is 
   * guarranteed to finish playing the sound as long as the program 
   * doesn't exit before it is done.  This method does not block, 
   * however.  So, if you invoke <code>play()</code> multiple times 
   * in a row, sounds will simply play on 
   * top of eachother - "accidental mixing"
   *
   * @see Playback
   */
  public void play() {
    // create the thread, add it to the Vector, and start it
    Playback playback = new Playback(this);
    playbacks.add(playback);
    playback.start(); 
  }
  
    /**
   * Deletes the specified playback object from the Vector.  This 
   * should only be called from within the run() method of an 
   * individual playback thread.  
   *
   * @see Playback#run()
   */
  
  public void removePlayback(Playback playbackToRemove) {
    if(playbacks.contains(playbackToRemove)) {
      playbacks.remove(playbackToRemove);
      playbackToRemove = null;
    }
  }
  
  /**************************************************************************/
  /************************** ACCESSING SOUND INFO **************************/
  /**************************************************************************/
  
  
  /**
   * Returns the number of samples in this sound
   * @return the number of sample frames
   */
  public int getNumSamples() {
    return audioFileFormat.getFrameLength();
  }
  
  
  /**
   * If this is a mono sound, obtains the single sample contained 
   * within this frame, else obtains the first (left) sample 
   * contained in the specified frame.
   *
   * @param frameNum the index of the frame to access
   * @return an integer representation of the bytes contained within 
   * the specified frame
   * @throws SoundException if the frame number is invalid.
   */
  public int getSampleValue(int frameNum) {
    //Before we get started, lets make sure that frame exists
    if(frameNum >= audioFileFormat.getFrameLength()) {
      System.out.println("You are trying to access the sample at index: " + (frameNum) + ", but the last valid index is at " + (audioFileFormat.getFrameLength() - 1));
    } else if(frameNum < 0) {
      System.out.println("You asked for the sample at index: " + (frameNum) + ".  This number is less than zero.  Please try again using an index in the range [0," + (audioFileFormat.getFrameLength() - 1) +"]");
    }
   
    AudioFormat format = audioFileFormat.getFormat();
    int sampleSizeInBits = format.getSampleSizeInBits();
    boolean isBigEndian = format.isBigEndian();
    
    byte[] theFrame = getFrame(frameNum);
    
    if(format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) {
      //since we're always returning the left sample, 
      //we don't care if we're mono or stereo, left is
      //always first in the frame
      if(sampleSizeInBits == 8)//8 bits == 1 byte
        return theFrame[0];
      else if(sampleSizeInBits == 16)
        return TConversionTool.bytesToInt16(theFrame, 0, isBigEndian);
      else if(sampleSizeInBits == 24)
        return TConversionTool.bytesToInt24(theFrame, 0, isBigEndian);
      else if(sampleSizeInBits == 32)
        return TConversionTool.bytesToInt32(theFrame, 0, isBigEndian);
      else {
        System.out.println("Unsupported audio encoding.  The sample size is not recognized as a standard format.");
        return -1;
      }
    } else if(format.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED)) {
      if(sampleSizeInBits == 8)
        return TConversionTool.unsignedByteToInt(theFrame[0]) - (int)Math.pow(2,7);
      else if(sampleSizeInBits == 16)
        return TConversionTool.unsignedByteToInt16(theFrame, 0, isBigEndian) - (int)Math.pow(2, 15);
      else if(sampleSizeInBits == 24)
        return TConversionTool.unsignedByteToInt24(theFrame, 0, isBigEndian) - (int)Math.pow(2, 23);
      else if(sampleSizeInBits == 32)
        return TConversionTool.unsignedByteToInt32(theFrame, 0, isBigEndian) - (int)Math.pow(2, 31);
      else {
        System.out.println("Unsupported audio encoding.  The sample size is not recognized as a standard format.");
        return -1;
      }
    } else if(format.getEncoding().equals(AudioFormat.Encoding.ALAW)) {
      return TConversionTool.alaw2linear(buffer[0]);
    } else if(format.getEncoding().equals(AudioFormat.Encoding.ULAW)) {
      return TConversionTool.ulaw2linear(buffer[0]);
    } else {
      System.out.println("unsupported audio encoding: " + format.getEncoding() + ".  Currently only PCM, ALAW and ULAW are supported.  Please try again with a different file.");
      return -1;
    }
  }  
  
  /**
   * Returns an array containing all of the bytes in the specified 
   * frame.
   * 
   * @param frameNum the index of the frame to access
   * @return the array containing all of the bytes in frame 
   *         <code>frameNum</code>
   * @throws SoundException if the frame number is invalid.
   */
  public byte[] getFrame(int frameNum)
  {
    if(frameNum >= audioFileFormat.getFrameLength())
    {
      System.out.println("That index "+ (frameNum) +", does not exist. "+ 
                 "The last valid index is "+ 
                 (audioFileFormat.getFrameLength() -1));
    }
    
    int frameSize = audioFileFormat.getFormat().getFrameSize();
    byte[] theFrame = new byte[frameSize];
    for (int i = 0; i < frameSize; i++)
    {
      theFrame[i] = buffer[frameNum*frameSize+i];
    }
    return theFrame;
  }
  

  /**
   * Method to report an index exception for this sound
   */
  private void reportIndexException(int index, Exception ex)
  {
    System.out.println("The index " + index + 
                       " isn't valid for this sound");
  }
  
  
  /**************************************************************************/
  /************************** CHANGING THE SOUND ****************************/
  /**************************************************************************/
  
   /**
   * Changes the value of the sample found at the specified frame.  If this
   * sound has more than one channel, then this defaults to setting only the
   * first (left) sample.
   *
   * @param frameNum the index of the frame where the sample should be changed
   * @param sample an int representation of the new sample to put in this
   *               sound's buffer at the specified frame
   * @throws SoundException if the frameNumber is invalid, or
   *                            another problem is encountered
   */
 public void setSampleValue(int frameNum, int sampleValue)
  {
    AudioFormat format = audioFileFormat.getFormat();
    int sampleSizeInBits = format.getSampleSizeInBits();
    boolean isBigEndian = format.isBigEndian();
    
    byte[] theFrame = getFrame(frameNum);
    
    if(format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED))
    {
      if(sampleSizeInBits == 8)//8 bits = 1 byte = first cell in array
      {
        theFrame[0] = (byte)sampleValue;
        setFrame(frameNum, theFrame);
      }
      else if(sampleSizeInBits == 16)//2 bytes, first 2 cells in array
      {
        TConversionTool.intToBytes16(sampleValue, theFrame, 0, isBigEndian);
        setFrame(frameNum, theFrame);
      }
      else if(sampleSizeInBits == 24)
      {
        TConversionTool.intToBytes24(sampleValue, theFrame, 0, isBigEndian);
        setFrame(frameNum, theFrame);
      }
      else if(sampleSizeInBits == 32)
      {
        TConversionTool.intToBytes32(sampleValue, theFrame, 0, isBigEndian);
        setFrame(frameNum, theFrame);
      }
      else
      {
        System.out.println("Unsupported audio encoding.  The sample"+
                   "size is not recognized as a standard format");
      }
    }//if format == PCM_SIGNED
    else if(format.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED))
    {
      if(sampleSizeInBits == 8)
      {
        theFrame[0] = TConversionTool.intToUnsignedByte(sampleValue);
        setFrame(frameNum, theFrame);
      }
      else if(sampleSizeInBits == 16)
      {
        TConversionTool.intToUnsignedBytes16(sampleValue, theFrame, 0, isBigEndian);
        setFrame(frameNum, theFrame);
      }
      else if(sampleSizeInBits == 24)
      {
        TConversionTool.intToUnsignedBytes24(sampleValue, theFrame, 0, isBigEndian);
        setFrame(frameNum, theFrame);
      }
      else if(sampleSizeInBits == 32)
      {
        TConversionTool.intToUnsignedBytes32(sampleValue, theFrame, 0, isBigEndian);
        setFrame(frameNum, theFrame);
      }
      
      else
      {
        System.out.println("Unsupported audio encoding.  The sample"+
                   " size is not recognized as a standard "+
                   "format.");
      }
    }
    else if(format.getEncoding().equals(AudioFormat.Encoding.ALAW))
    {
      if((sampleValue>Short.MAX_VALUE)||(sampleValue<Short.MIN_VALUE))
        System.out.println("You are trying to set the sample value to: "+
                   sampleValue + ", but the maximum value for a sample"+
                   " in this format is: "+Short.MAX_VALUE+
                   ", and the minimum value is: "+Short.MIN_VALUE+
                   ".  Please choose a value in that range.");
      theFrame[0] = TConversionTool.linear2alaw((short)sampleValue);
      setFrame(frameNum, theFrame);
    }
    else if(format.getEncoding().equals(AudioFormat.Encoding.ULAW))
    {
      
      if((sampleValue>Short.MAX_VALUE)||(sampleValue<Short.MIN_VALUE))
        System.out.println("You are trying to set the sample value to: "+
                   sampleValue + ", but the maximum value for a sample"+
                   " in this format is: "+Short.MAX_VALUE+
                   ", and the minimum value is: "+Short.MIN_VALUE+
                   ".  Please choose a value in that range.");
      theFrame[0] = TConversionTool.linear2ulaw((short)sampleValue);
      setFrame(frameNum, theFrame);
    }
    else
    {
      System.out.println("unsupported audio encoding: " + 
                 format.getEncoding() + ".  Currently only PCM, " +
                 "ALAW and ULAW are supported.  Please try again" +
                 "with a different file.");
    }
  }//setSample(int, int)
  

  /**
   * Changes the value of each byte of the specified frame.
   * 
   * @param frameNum the index of the frame to change
   * @param theFrame the byte array that will be copied into this sound's
   *                 buffer in place of the specified frame.
   *@throws SoundException if the frameNumber is invalid.
   */
  public void setFrame(int frameNum, byte[] theFrame)
  {
    if(frameNum >= audioFileFormat.getFrameLength())
    {
      System.out.println("That frame, number "+frameNum+", does not exist. "+
                 "The last valid frame number is " + 
                 (audioFileFormat.getFrameLength() - 1));
    }
    int frameSize = audioFileFormat.getFormat().getFrameSize();
    if(frameSize != theFrame.length)
      System.out.println("Frame size doesn't match, line 383.  This should" +
                 " never happen.  Please report the problem to a TA.");
    for(int i = 0; i < frameSize; i++)
    {
      buffer[frameNum*frameSize+i] = theFrame[i];
    }
  }
  
  
 /**
   * Method to convert a mp3 sound into a wav sound
   * @param mp3File
   * @param wavFile
   */
//  public static void convert(String mp3File, String wavFile)
//  {
//    try {
//      Converter converter = new Converter();
//      converter.convert(mp3File,wavFile);
//    } catch (Exception ex) {
//      System.out.println("Couldn't covert the file " + mp3File);
//    }
//  }
 
} // end of SimpleSound class