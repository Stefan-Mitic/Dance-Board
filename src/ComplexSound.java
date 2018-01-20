public class ComplexSound extends SimpleSound {
  
  ////////////////////////// constructor /////////////////////
  /**
   * Constructs a new ComplexSound from the given file.
   * @param fileName The File from which to create this sound.
   * @see SimpleSound#loadFromFile(String filename)
   */
  public ComplexSound(String fileName) {
      // load the sound from the file
      super(fileName);
  }
  
  
  public void changeAmplitude(double factor){
  }
  
  public void changeFrequency(double factor){
  }
   
  public void reverse() {
  }
    
  public void echo(int delay) {
  }
  
  
}