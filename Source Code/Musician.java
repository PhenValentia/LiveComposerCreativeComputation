import javax.sound.midi.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Musician extends JFrame
      implements ActionListener, WindowListener {
  //UI Variables
  public JButton playBtn;
  public JButton pauseBtn;
  public JButton resetBtn;
  public JButton saveBtn;
  public String moodName = "Major";

  //File Variables
  int fileNum;

  //Midi Variables
  boolean runnableMade = false;
  Sequencer sequencer;
  Sequence sequence;

  int bpm = 220;
  int lastNote = 60;
  int lastTick = 0;
  boolean generateMusic = false;

  //Note Variables
  int[] majorScale = {60, 62, 64, 65, 67, 69, 71};
  int[] minorScale = {60, 62, 63, 65, 67, 68, 70};
  int[] bluesScale = {60, 63, 65, 66, 67, 70};

  public Musician() {
    setLayout(new FlowLayout());
    playBtn = new JButton("Play");   //Construct the Play Button
    add(playBtn);   //Add the Play Button to the UI
    pauseBtn = new JButton("Pause");   //Construct the Pause Button
    add(pauseBtn);   //Add the Pause Button to the UI
    resetBtn = new JButton("Reset");   //Construct the Reset Button
    add(resetBtn);   //Add the Reset Button to the UI
    saveBtn = new JButton("Save Created Song");   //Construct the Reset Button
    add(saveBtn);   //Add the Reset Button to the UI
    JComboBox mood = new JComboBox(new String[] { "Major", "Minor", "Blues" }); //Construct the Mood Selector
    add(mood);  //Add the Mood Selector to the UI

    addWindowListener(this);  //Add Listener to Frame Buttons

    setTitle("Live Music Composer");
    setSize(525, 75);
    setVisible(true);


    //CONSTRUCT MIDI
    try{
      //Create a Sequencer
      sequencer = MidiSystem.getSequencer();
      sequencer.open();

      //Create a Sequence
      sequence = new Sequence(Sequence.PPQ, 4);

      //Create a Track for Midi Events
      Track track = sequence.createTrack();

      //Create Initial Major Tune
      createRandomBar(track, lastTick, lastNote, moodName);

      //Create a RunnableComposer to continue making new music.
      Runnable runnableComposer =
      new Runnable(){
          public void run(){
            while (generateMusic){
              try{
                //System.out.println("Tick: " + sequencer.getTickPosition() + " + LastTick: " + lastTick);
                if(sequencer.getTickPosition() > (lastTick - 16))
                {
                  System.out.println("Create Random Bar.");
                  createRandomBar(track, lastTick, lastNote, moodName);
                }
              }
              catch (Exception ex) {
                  ex.printStackTrace();
              }
            }
          }
      };

      //Enter Sequence into Sequencer
      sequencer.setSequence(sequence);

      //Set Beats Per Minute of Song
      sequencer.setTempoInBPM(bpm);

      //Add Action Listeners
      playBtn.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent evt) {
          sequencer.setTempoInBPM(bpm);
          sequencer.start();
          generateMusic = true;
          if(runnableMade == false){
            Thread compThread = new Thread(runnableComposer);
            compThread.start();
            runnableMade = true;
          }
        }
      });  //Add Listener to Play Button

      pauseBtn.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent evt) {
          sequencer.setTempoInBPM(bpm);
          sequencer.stop();
          generateMusic = false;
        }
      });  //Add Listener to pause Button

      resetBtn.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent evt) {
          sequencer.setTickPosition(1);
          sequencer.setTempoInBPM(bpm);
        }
      });  //Add Listener to Reset Button

      saveBtn.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent evt) {
          try{
            File f = new File("song_"+fileNum+".mid");
		        MidiSystem.write(sequence,1,f);
            fileNum++;
          }
          catch (Exception ex) {
                ex.printStackTrace();
          }
        }
      });  //Add Listener to Save Button

      mood.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent evt) {
          moodName = (String)mood.getSelectedItem();
        }
      }); //Add Listener to Combo Box
    }
    catch (Exception ex) {
          ex.printStackTrace();
    }
  }

  //Create a Midi Event
  public MidiEvent makeMidiEvent(int command, int channel, int note, int velocity, int tick)
  {
      MidiEvent event = null;
      try {

          //ShortMessage stores a note as command type, channel, instrument it has to be played on and its speed
          ShortMessage a = new ShortMessage();
          a.setMessage(command, channel, note, velocity);

          //A midi event set with a short message (representing a note) and the tick at which the note will be played
          event = new MidiEvent(a, tick);
      }
      catch (Exception ex) {
          ex.printStackTrace();
      }
      return event;
  }

  //Run Upon Instance Made
  public static void main (String[] args) {
    //Make sure it all works.
    MidiDevice.Info[] devices = MidiSystem.getMidiDeviceInfo();
      if (devices.length == 0) {
        System.out.println("No MIDI devices found. Please Install a Midi Device and Soundbank");
      } else {
        for (MidiDevice.Info dev : devices) {
          System.out.println(dev);
      }
    }
    try{
      System.out.println(MidiSystem.getReceiver());
    }
    catch (Exception ex) {
          ex.printStackTrace();
          System.out.println();
          System.out.println("Please Download Java SE14");
          System.out.println();
    }
      System.out.println("Musician Initialised.");

      //Initialise Musician Instance
      Musician setUpMusician = new Musician();
  }

    //Create a 4-4 Bar based on the last note and current mood
    public void createRandomBar(Track track, int time, int lastNote, String mood)
    {
      //Choose Melody
      Random rng = new Random();
      int nextNote = lastNote;
      if (rng.nextFloat() < 0.8)
      {
        if (mood == "Major"){
          nextNote = getNextMajorNote(nextNote);
        } else if (mood == "Minor"){
          nextNote = getNextMinorNote(nextNote);
        } else if (mood == "Blues"){
          nextNote = getNextBluesNote(nextNote);
        }
        track.add(makeMidiEvent(144, 1, nextNote, 100, time+4));
        track.add(makeMidiEvent(128, 1, nextNote, 100, time+4));
        System.out.println("Current Note: " + nextNote + " Tick 4");
      }

      //Add Chords
      track.add(makeMidiEvent(144, 1, nextNote+12, 60, time+4));
      track.add(makeMidiEvent(144, 1, nextNote-12, 60, time+4));
      track.add(makeMidiEvent(128, 1, nextNote+12, 60, time+6));
      track.add(makeMidiEvent(128, 1, nextNote-12, 60, time+6));

      //Keep Choosing Melody
      if (rng.nextFloat() < 0.9)
      {
        if (mood == "Major"){
          nextNote = getNextMajorNote(nextNote);
        } else if (mood == "Minor"){
          nextNote = getNextMinorNote(nextNote);
        } else if (mood == "Blues"){
          nextNote = getNextBluesNote(nextNote);
        }
        track.add(makeMidiEvent(144, 1, nextNote, 100, time+8));
        track.add(makeMidiEvent(128, 1, nextNote, 100, time+8));
        System.out.println("Current Note: " + nextNote + " Tick 8");
      }
      if (rng.nextFloat() < 0.8)
      {
        if (mood == "Major"){
          nextNote = getNextMajorNote(nextNote);
        } else if (mood == "Minor"){
          nextNote = getNextMinorNote(nextNote);
        } else if (mood == "Blues"){
          nextNote = getNextBluesNote(nextNote);
        }
        track.add(makeMidiEvent(144, 1, nextNote, 100, time+12));
        track.add(makeMidiEvent(128, 1, nextNote, 100, time+12));
        System.out.println("Current Note: " + nextNote + " Tick 12");
      }
      if (rng.nextFloat() < 0.5)
      {
        if (mood == "Major"){
          nextNote = getNextMajorNote(nextNote);
        } else if (mood == "Minor"){
          nextNote = getNextMinorNote(nextNote);
        } else if (mood == "Blues"){
          nextNote = getNextBluesNote(nextNote);
        }
        track.add(makeMidiEvent(144, 1, nextNote, 100, time+16));
        track.add(makeMidiEvent(128, 1, nextNote, 100, time+16));
        System.out.println("Current Note: " + nextNote + " Tick 16");
      }
      lastTick = time + 16;
    }

    //Generate the next chord in the major scale
    public int[] getNextMajorChord()
    {
      Random rng = new Random();
      int startNote = rng.nextInt(majorScale.length)-1;
      int chordNote1 = majorScale[startNote];
      int chordNote2 = majorScale[startNote];
      int chordNote3 = majorScale[startNote];
      if(startNote+2 > majorScale.length)
      {
        chordNote2 = majorScale[startNote+1-majorScale.length];
      }
      else{
        chordNote2 = majorScale[startNote+1];
      }
      if(startNote+3 > majorScale.length)
      {
        chordNote3 = majorScale[startNote+2-majorScale.length];
      }
      else{
        chordNote3 = majorScale[startNote+2];
      }
      int[] returnChord = {chordNote1, chordNote2, chordNote3};
      return returnChord;
    }

    //Generate the next chord in the minor scale
    public int[] getNextMinorChord()
    {
      Random rng = new Random();
      int startNote = rng.nextInt(minorScale.length)-1;
      int chordNote1 = minorScale[startNote];
      int chordNote2 = minorScale[startNote];
      int chordNote3 = minorScale[startNote];
      if(startNote+2 > minorScale.length)
      {
        chordNote2 = minorScale[startNote+1-minorScale.length];
      }
      else{
        chordNote2 = minorScale[startNote+1];
      }
      if(startNote+3 > minorScale.length)
      {
        chordNote3 = minorScale[startNote+2-minorScale.length];
      }
      else{
        chordNote3 = minorScale[startNote+2];
      }
      int[] returnChord = {chordNote1, chordNote2, chordNote3};
      return returnChord;
    }

    //Generate the next chord in the blues scale
    public int[] getNextBluesChord()
    {
      Random rng = new Random();
      int startNote = rng.nextInt(bluesScale.length)-1;
      int chordNote1 = bluesScale[startNote];
      int chordNote2 = bluesScale[startNote];
      int chordNote3 = bluesScale[startNote];
      if(startNote+2 > bluesScale.length)
      {
        chordNote2 = bluesScale[startNote+1-bluesScale.length];
      }
      else{
        chordNote2 = bluesScale[startNote+1];
      }
      if(startNote+3 > bluesScale.length)
      {
        chordNote3 = bluesScale[startNote+2-bluesScale.length];
      }
      else{
        chordNote3 = bluesScale[startNote+2];
      }
      int[] returnChord = {chordNote1, chordNote2, chordNote3};
      return returnChord;
    }

    //Generate the next note in the Major Scale
    public int getNextMajorNote (int note)
    {
      int nextNote = note;

      //If the note is C, generate next note.
      if ((note%12) == 0)
      {
        Random rng = new Random();
        int checkNote = rng.nextInt(35); //Above 28, the note remains the same.
        if (checkNote < 3)
        {
          nextNote = note - 12;
        }
        else if (checkNote < 6)
        {
          nextNote = note + 12;
        }
        else if (checkNote < 7)
        {
          nextNote = note - 10;
        }
        else if (checkNote < 8)
        {
          nextNote = note + 11;
        }
        else if (checkNote < 9)
        {
          nextNote = note - 8;
        }
        else if (checkNote < 10)
        {
          nextNote = note + 9;
        }
        else if (checkNote < 11)
        {
          nextNote = note - 7;
        }
        else if (checkNote < 12)
        {
          nextNote = note + 7;
        }
        else if (checkNote < 13)
        {
          nextNote = note - 5;
        }
        else if (checkNote < 14)
        {
          nextNote = note + 5;
        }
        else if (checkNote < 16)
        {
          nextNote = note - 3;
        }
        else if (checkNote < 18)
        {
          nextNote = note + 4;
        }
        else if (checkNote < 23)
        {
          nextNote = note - 1;
        }
        else if (checkNote < 28)
        {
          nextNote = note + 2;
        }
      }

      //If the note is D, generate next note.
      if ((note%12) == 2)
      {
        Random rng = new Random();
        int checkNote = rng.nextInt(35); //Above 28, the note remains the same.
        if (checkNote < 3)
        {
          nextNote = note - 12;
        }
        else if (checkNote < 6)
        {
          nextNote = note + 12;
        }
        else if (checkNote < 7)
        {
          nextNote = note - 10;
        }
        else if (checkNote < 8)
        {
          nextNote = note + 10;
        }
        else if (checkNote < 9)
        {
          nextNote = note - 9;
        }
        else if (checkNote < 10)
        {
          nextNote = note + 9;
        }
        else if (checkNote < 11)
        {
          nextNote = note - 7;
        }
        else if (checkNote < 12)
        {
          nextNote = note + 7;
        }
        else if (checkNote < 13)
        {
          nextNote = note - 5;
        }
        else if (checkNote < 14)
        {
          nextNote = note + 5;
        }
        else if (checkNote < 16)
        {
          nextNote = note - 3;
        }
        else if (checkNote < 18)
        {
          nextNote = note + 3;
        }
        else if (checkNote < 23)
        {
          nextNote = note - 2;
        }
        else if (checkNote < 28)
        {
          nextNote = note + 2;
        }
      }


      //If the note is E, generate next note.
      if ((note%12) == 4)
      {
        Random rng = new Random();
        int checkNote = rng.nextInt(35); //Above 28, the note remains the same.
        if (checkNote < 3)
        {
          nextNote = note - 12;//
        }
        else if (checkNote < 6)
        {
          nextNote = note + 12;//
        }
        else if (checkNote < 7)
        {
          nextNote = note - 11;//
        }
        else if (checkNote < 8)
        {
          nextNote = note + 10;//
        }
        else if (checkNote < 9)
        {
          nextNote = note - 9;//
        }
        else if (checkNote < 10)
        {
          nextNote = note + 8;//
        }
        else if (checkNote < 11)
        {
          nextNote = note - 7;//
        }
        else if (checkNote < 12)
        {
          nextNote = note + 7;//
        }
        else if (checkNote < 13)
        {
          nextNote = note - 5;//
        }
        else if (checkNote < 14)
        {
          nextNote = note + 5;//
        }
        else if (checkNote < 16)
        {
          nextNote = note - 4;//
        }
        else if (checkNote < 18)
        {
          nextNote = note + 3;//
        }
        else if (checkNote < 23)
        {
          nextNote = note - 2;//
        }
        else if (checkNote < 28)
        {
          nextNote = note + 1;//
        }
      }


      //If the note is F, generate next note.
      if ((note%12) == 5)
      {
        Random rng = new Random();
        int checkNote = rng.nextInt(35); //Above 28, the note remains the same.
        if (checkNote < 3)
        {
          nextNote = note - 12;//
        }
        else if (checkNote < 6)
        {
          nextNote = note + 12;//
        }
        else if (checkNote < 7)
        {
          nextNote = note - 10;//
        }
        else if (checkNote < 8)
        {
          nextNote = note + 11;//
        }
        else if (checkNote < 9)
        {
          nextNote = note - 8;//
        }
        else if (checkNote < 10)
        {
          nextNote = note + 9;//
        }
        else if (checkNote < 11)
        {
          nextNote = note - 6;//
        }
        else if (checkNote < 12)
        {
          nextNote = note + 7;//
        }
        else if (checkNote < 13)
        {
          nextNote = note - 5;//
        }
        else if (checkNote < 14)
        {
          nextNote = note + 6;//
        }
        else if (checkNote < 16)
        {
          nextNote = note - 3;//
        }
        else if (checkNote < 18)
        {
          nextNote = note + 4;//
        }
        else if (checkNote < 23)
        {
          nextNote = note - 1;//
        }
        else if (checkNote < 28)
        {
          nextNote = note + 2;//
        }
      }


      //If the note is G, generate next note.
      if ((note%12) == 7)
      {
        Random rng = new Random();
        int checkNote = rng.nextInt(35); //Above 28, the note remains the same.
        if (checkNote < 3)
        {
          nextNote = note - 12;//
        }
        else if (checkNote < 6)
        {
          nextNote = note + 12;//
        }
        else if (checkNote < 7)
        {
          nextNote = note - 10;//
        }
        else if (checkNote < 8)
        {
          nextNote = note + 10;//
        }
        else if (checkNote < 9)
        {
          nextNote = note - 8;//
        }
        else if (checkNote < 10)
        {
          nextNote = note + 9;//
        }
        else if (checkNote < 11)
        {
          nextNote = note - 7;//
        }
        else if (checkNote < 12)
        {
          nextNote = note + 7;//
        }
        else if (checkNote < 13)
        {
          nextNote = note - 5;//
        }
        else if (checkNote < 14)
        {
          nextNote = note + 5;//
        }
        else if (checkNote < 16)
        {
          nextNote = note - 3;//
        }
        else if (checkNote < 18)
        {
          nextNote = note + 4;//
        }
        else if (checkNote < 23)
        {
          nextNote = note - 2;//
        }
        else if (checkNote < 28)
        {
          nextNote = note + 2;//
        }
      }


      //If the note is A, generate next note.
      if ((note%12) == 9)
      {
        Random rng = new Random();
        int checkNote = rng.nextInt(35); //Above 28, the note remains the same.
        if (checkNote < 3)
        {
          nextNote = note - 12;//
        }
        else if (checkNote < 6)
        {
          nextNote = note + 12;//
        }
        else if (checkNote < 7)
        {
          nextNote = note - 10;//
        }
        else if (checkNote < 8)
        {
          nextNote = note + 10;//
        }
        else if (checkNote < 9)
        {
          nextNote = note - 9;//
        }
        else if (checkNote < 10)
        {
          nextNote = note + 8;//
        }
        else if (checkNote < 11)
        {
          nextNote = note - 7;//
        }
        else if (checkNote < 12)
        {
          nextNote = note + 7;//
        }
        else if (checkNote < 13)
        {
          nextNote = note - 5;//
        }
        else if (checkNote < 14)
        {
          nextNote = note + 5;//
        }
        else if (checkNote < 16)
        {
          nextNote = note - 4;//
        }
        else if (checkNote < 18)
        {
          nextNote = note + 3;//
        }
        else if (checkNote < 23)
        {
          nextNote = note - 2;//
        }
        else if (checkNote < 28)
        {
          nextNote = note + 2;//
        }
      }


      //If the note is B, generate next note.
      if ((note%12) == 11)
      {
        Random rng = new Random();
        int checkNote = rng.nextInt(35); //Above 28, the note remains the same.
        if (checkNote < 3)
        {
          nextNote = note - 12;//
        }
        else if (checkNote < 6)
        {
          nextNote = note + 12;//
        }
        else if (checkNote < 7)
        {
          nextNote = note - 11;//
        }
        else if (checkNote < 8)
        {
          nextNote = note + 10;//
        }
        else if (checkNote < 9)
        {
          nextNote = note - 9;//
        }
        else if (checkNote < 10)
        {
          nextNote = note + 8;//
        }
        else if (checkNote < 11)
        {
          nextNote = note - 7;//
        }
        else if (checkNote < 12)
        {
          nextNote = note + 6;//
        }
        else if (checkNote < 13)
        {
          nextNote = note - 6;//
        }
        else if (checkNote < 14)
        {
          nextNote = note + 5;//
        }
        else if (checkNote < 16)
        {
          nextNote = note - 4;//
        }
        else if (checkNote < 18)
        {
          nextNote = note + 3;//
        }
        else if (checkNote < 23)
        {
          nextNote = note - 2;//
        }
        else if (checkNote < 28)
        {
          nextNote = note + 1;//
        }
      }

      if (nextNote < 1) //Make sure no negative notes.
      {
        nextNote = nextNote + 12;
      }

      return nextNote;
    }

    //Get new note in C Minor Scale.
    public int getNextMinorNote (int note)
    {
      int nextNote = note;

      //If the note is C, generate next note.
      if ((note%12) == 0)
      {
        Random rng = new Random();
        int checkNote = rng.nextInt(35); //Above 28, the note remains the same.
        if (checkNote < 3)
        {
          nextNote = note - 12;//
        }
        else if (checkNote < 6)
        {
          nextNote = note + 12;//
        }
        else if (checkNote < 7)
        {
          nextNote = note - 10;//
        }
        else if (checkNote < 8)
        {
          nextNote = note + 10;//
        }
        else if (checkNote < 9)
        {
          nextNote = note - 9;//
        }
        else if (checkNote < 10)
        {
          nextNote = note + 8;//
        }
        else if (checkNote < 11)
        {
          nextNote = note - 7;//
        }
        else if (checkNote < 12)
        {
          nextNote = note + 7;//
        }
        else if (checkNote < 13)
        {
          nextNote = note - 5;//
        }
        else if (checkNote < 14)
        {
          nextNote = note + 5;//
        }
        else if (checkNote < 16)
        {
          nextNote = note - 4;
        }
        else if (checkNote < 18)
        {
          nextNote = note + 3;
        }
        else if (checkNote < 23)
        {
          nextNote = note - 2;
        }
        else if (checkNote < 28)
        {
          nextNote = note + 2;
        }
      }

      //If the note is D, generate next note.
      if ((note%12) == 2)
      {
        Random rng = new Random();
        int checkNote = rng.nextInt(35); //Above 28, the note remains the same.
        if (checkNote < 3)
        {
          nextNote = note - 12;
        }
        else if (checkNote < 6)
        {
          nextNote = note + 12;
        }
        else if (checkNote < 7)
        {
          nextNote = note - 11;
        }
        else if (checkNote < 8)
        {
          nextNote = note + 10;
        }
        else if (checkNote < 9)
        {
          nextNote = note - 9;
        }
        else if (checkNote < 10)
        {
          nextNote = note + 8;
        }
        else if (checkNote < 11)
        {
          nextNote = note - 7;
        }
        else if (checkNote < 12)
        {
          nextNote = note + 6;
        }
        else if (checkNote < 13)
        {
          nextNote = note - 6;
        }
        else if (checkNote < 14)
        {
          nextNote = note + 5;
        }
        else if (checkNote < 16)
        {
          nextNote = note - 4;
        }
        else if (checkNote < 18)
        {
          nextNote = note + 3;
        }
        else if (checkNote < 23)
        {
          nextNote = note - 2;
        }
        else if (checkNote < 28)
        {
          nextNote = note + 1;
        }
      }

      //If the note is d#, generate next note.
      if ((note%12) == 3)
      {
        Random rng = new Random();
        int checkNote = rng.nextInt(35); //Above 28, the note remains the same.
        if (checkNote < 3)
        {
          nextNote = note - 12;//
        }
        else if (checkNote < 6)
        {
          nextNote = note + 12;//
        }
        else if (checkNote < 7)
        {
          nextNote = note - 10;//
        }
        else if (checkNote < 8)
        {
          nextNote = note + 11;//
        }
        else if (checkNote < 9)
        {
          nextNote = note - 8;//
        }
        else if (checkNote < 10)
        {
          nextNote = note + 9;//
        }
        else if (checkNote < 11)
        {
          nextNote = note - 7;//
        }
        else if (checkNote < 12)
        {
          nextNote = note + 7;//
        }
        else if (checkNote < 13)
        {
          nextNote = note - 5;//
        }
        else if (checkNote < 14)
        {
          nextNote = note + 5;//
        }
        else if (checkNote < 16)
        {
          nextNote = note - 3;//
        }
        else if (checkNote < 18)
        {
          nextNote = note + 4;//
        }
        else if (checkNote < 23)
        {
          nextNote = note - 1;//
        }
        else if (checkNote < 28)
        {
          nextNote = note + 2;//
        }
      }

      //If the note is F, generate next note.
      if ((note%12) == 5)
      {
        Random rng = new Random();
        int checkNote = rng.nextInt(35); //Above 28, the note remains the same.
        if (checkNote < 3)
        {
          nextNote = note - 12;//
        }
        else if (checkNote < 6)
        {
          nextNote = note + 12;//
        }
        else if (checkNote < 7)
        {
          nextNote = note - 10;//
        }
        else if (checkNote < 8)
        {
          nextNote = note + 10;//
        }
        else if (checkNote < 9)
        {
          nextNote = note - 9;//
        }
        else if (checkNote < 10)
        {
          nextNote = note + 9;//
        }
        else if (checkNote < 11)
        {
          nextNote = note - 7;//
        }
        else if (checkNote < 12)
        {
          nextNote = note + 7;//
        }
        else if (checkNote < 13)
        {
          nextNote = note - 5;//
        }
        else if (checkNote < 14)
        {
          nextNote = note + 5;//
        }
        else if (checkNote < 16)
        {
          nextNote = note - 3;//
        }
        else if (checkNote < 18)
        {
          nextNote = note + 3;//
        }
        else if (checkNote < 23)
        {
          nextNote = note - 2;//
        }
        else if (checkNote < 28)
        {
          nextNote = note + 2;//
        }
      }


      //If the note is G, generate next note.
      if ((note%12) == 7)
      {
        Random rng = new Random();
        int checkNote = rng.nextInt(35); //Above 28, the note remains the same.
        if (checkNote < 3)
        {
          nextNote = note - 12;//
        }
        else if (checkNote < 6)
        {
          nextNote = note + 12;//
        }
        else if (checkNote < 7)
        {
          nextNote = note - 11;//
        }
        else if (checkNote < 8)
        {
          nextNote = note + 10;//
        }
        else if (checkNote < 9)
        {
          nextNote = note - 9;//
        }
        else if (checkNote < 10)
        {
          nextNote = note + 8;//
        }
        else if (checkNote < 11)
        {
          nextNote = note - 7;//
        }
        else if (checkNote < 12)
        {
          nextNote = note + 7;//
        }
        else if (checkNote < 13)
        {
          nextNote = note - 5;//
        }
        else if (checkNote < 14)
        {
          nextNote = note + 5;//
        }
        else if (checkNote < 16)
        {
          nextNote = note - 4;//
        }
        else if (checkNote < 18)
        {
          nextNote = note + 3;//
        }
        else if (checkNote < 23)
        {
          nextNote = note - 2;//
        }
        else if (checkNote < 28)
        {
          nextNote = note + 1;//
        }
      }


      //If the note is G#, generate next note.
      if ((note%12) == 8)
      {
        Random rng = new Random();
        int checkNote = rng.nextInt(35); //Above 28, the note remains the same.
        if (checkNote < 3)
        {
          nextNote = note - 12;//
        }
        else if (checkNote < 6)
        {
          nextNote = note + 12;//
        }
        else if (checkNote < 7)
        {
          nextNote = note - 10;//
        }
        else if (checkNote < 8)
        {
          nextNote = note + 11;//
        }
        else if (checkNote < 9)
        {
          nextNote = note - 8;//
        }
        else if (checkNote < 10)
        {
          nextNote = note + 9;//
        }
        else if (checkNote < 11)
        {
          nextNote = note - 6;//
        }
        else if (checkNote < 12)
        {
          nextNote = note + 7;//
        }
        else if (checkNote < 13)
        {
          nextNote = note - 5;//
        }
        else if (checkNote < 14)
        {
          nextNote = note + 6;//
        }
        else if (checkNote < 16)
        {
          nextNote = note - 3;//
        }
        else if (checkNote < 18)
        {
          nextNote = note + 4;//
        }
        else if (checkNote < 23)
        {
          nextNote = note - 1;//
        }
        else if (checkNote < 28)
        {
          nextNote = note + 2;//
        }
      }


      //If the note is A#, generate next note.
      if ((note%12) == 10)
      {
        Random rng = new Random();
        int checkNote = rng.nextInt(35); //Above 28, the note remains the same.
        if (checkNote < 3)
        {
          nextNote = note - 12;//
        }
        else if (checkNote < 6)
        {
          nextNote = note + 12;//
        }
        else if (checkNote < 7)
        {
          nextNote = note - 10;//
        }
        else if (checkNote < 8)
        {
          nextNote = note + 10;//
        }
        else if (checkNote < 9)
        {
          nextNote = note - 8;//
        }
        else if (checkNote < 10)
        {
          nextNote = note + 9;//
        }
        else if (checkNote < 11)
        {
          nextNote = note - 7;//
        }
        else if (checkNote < 12)
        {
          nextNote = note + 7;//
        }
        else if (checkNote < 13)
        {
          nextNote = note - 5;//
        }
        else if (checkNote < 14)
        {
          nextNote = note + 5;//
        }
        else if (checkNote < 16)
        {
          nextNote = note - 3;//
        }
        else if (checkNote < 18)
        {
          nextNote = note + 4;//
        }
        else if (checkNote < 23)
        {
          nextNote = note - 2;//
        }
        else if (checkNote < 28)
        {
          nextNote = note + 2;//
        }
      }

      if (nextNote < 1) //Make sure no negative notes.
      {
        nextNote = nextNote + 12;
      }

      return nextNote;
    }

    //Get new note in the Blues Pentatonic Scale.
    public int getNextBluesNote (int note)
    {
      int nextNote = note;

      //If the note is C, generate next note.
      if ((note%12) == 0)
      {
        Random rng = new Random();
        int checkNote = rng.nextInt(33); //Above 25, the note remains the same.
        if (checkNote < 3)
        {
          nextNote = note - 12;//
        }
        else if (checkNote < 6)
        {
          nextNote = note + 12;//
        }
        else if (checkNote < 7)
        {
          nextNote = note - 9;//
        }
        else if (checkNote < 8)
        {
          nextNote = note + 10;//
        }
        else if (checkNote < 9)
        {
          nextNote = note - 7;//
        }
        else if (checkNote < 10)
        {
          nextNote = note + 7;//
        }
        else if (checkNote < 11)
        {
          nextNote = note - 6;//
        }
        else if (checkNote < 12)
        {
          nextNote = note + 6;//
        }
        else if (checkNote < 14)
        {
          nextNote = note - 5;//
        }
        else if (checkNote < 16)
        {
          nextNote = note + 5;//
        }
        else if (checkNote < 21)
        {
          nextNote = note - 2;//
        }
        else if (checkNote < 26)
        {
          nextNote = note + 3;//
        }
      }

      //If the note is d#, generate next note.
      if ((note%12) == 3)
      {
        Random rng = new Random();
        int checkNote = rng.nextInt(33); //Above 25, the note remains the same.
        if (checkNote < 3)
        {
          nextNote = note - 12;//
        }
        else if (checkNote < 6)
        {
          nextNote = note + 12;//
        }
        else if (checkNote < 7)
        {
          nextNote = note - 10;//
        }
        else if (checkNote < 8)
        {
          nextNote = note + 9;//
        }
        else if (checkNote < 9)
        {
          nextNote = note - 9;//
        }
        else if (checkNote < 10)
        {
          nextNote = note + 7;//
        }
        else if (checkNote < 11)
        {
          nextNote = note - 8;//
        }
        else if (checkNote < 12)
        {
          nextNote = note + 4;//
        }
        else if (checkNote < 14)
        {
          nextNote = note - 5;//
        }
        else if (checkNote < 16)
        {
          nextNote = note + 3;//
        }
        else if (checkNote < 21)
        {
          nextNote = note - 3;//
        }
        else if (checkNote < 26)
        {
          nextNote = note + 2;//
        }
      }

      //If the note is f, generate next note.
      if ((note%12) == 5)
      {
        Random rng = new Random();
        int checkNote = rng.nextInt(33); //Above 25, the note remains the same.
        if (checkNote < 3)
        {
          nextNote = note - 12;//
        }
        else if (checkNote < 6)
        {
          nextNote = note + 12;//
        }
        else if (checkNote < 7)
        {
          nextNote = note - 11;//
        }
        else if (checkNote < 8)
        {
          nextNote = note + 10;//
        }
        else if (checkNote < 9)
        {
          nextNote = note - 10;//
        }
        else if (checkNote < 10)
        {
          nextNote = note + 7;//
        }
        else if (checkNote < 11)
        {
          nextNote = note - 7;//
        }
        else if (checkNote < 12)
        {
          nextNote = note + 5;//
        }
        else if (checkNote < 14)
        {
          nextNote = note - 5;//
        }
        else if (checkNote < 16)
        {
          nextNote = note + 2;//
        }
        else if (checkNote < 21)
        {
          nextNote = note - 2;//
        }
        else if (checkNote < 26)
        {
          nextNote = note + 1;//
        }
      }

      //If the note is f#, generate next note.
      if ((note%12) == 6)
      {
        Random rng = new Random();
        int checkNote = rng.nextInt(33); //Above 25, the note remains the same.
        if (checkNote < 3)
        {
          nextNote = note - 12;//
        }
        else if (checkNote < 6)
        {
          nextNote = note + 12;//
        }
        else if (checkNote < 7)
        {
          nextNote = note - 11;//
        }
        else if (checkNote < 8)
        {
          nextNote = note + 11;//
        }
        else if (checkNote < 9)
        {
          nextNote = note - 8;//
        }
        else if (checkNote < 10)
        {
          nextNote = note + 9;//
        }
        else if (checkNote < 11)
        {
          nextNote = note - 6;//
        }
        else if (checkNote < 12)
        {
          nextNote = note + 6;//
        }
        else if (checkNote < 14)
        {
          nextNote = note - 3;//
        }
        else if (checkNote < 16)
        {
          nextNote = note + 4;//
        }
        else if (checkNote < 21)
        {
          nextNote = note - 1;//
        }
        else if (checkNote < 26)
        {
          nextNote = note + 1;//
        }
      }

      //If the note is g, generate next note.
      if ((note%12) == 7)
      {
        Random rng = new Random();
        int checkNote = rng.nextInt(33); //Above 25, the note remains the same.
        if (checkNote < 3)
        {
          nextNote = note - 12;//
        }
        else if (checkNote < 6)
        {
          nextNote = note + 12;//
        }
        else if (checkNote < 7)
        {
          nextNote = note - 9;//
        }
        else if (checkNote < 8)
        {
          nextNote = note + 11;//
        }
        else if (checkNote < 9)
        {
          nextNote = note - 7;//
        }
        else if (checkNote < 10)
        {
          nextNote = note + 10;//
        }
        else if (checkNote < 11)
        {
          nextNote = note - 4;//
        }
        else if (checkNote < 12)
        {
          nextNote = note + 8;//
        }
        else if (checkNote < 14)
        {
          nextNote = note - 2;//
        }
        else if (checkNote < 16)
        {
          nextNote = note + 5;//
        }
        else if (checkNote < 21)
        {
          nextNote = note - 1;//
        }
        else if (checkNote < 26)
        {
          nextNote = note + 3;//
        }
      }

      //If the note is a#, generate next note.
      if ((note%12) == 10)
      {
        Random rng = new Random();
        int checkNote = rng.nextInt(33); //Above 25, the note remains the same.
        if (checkNote < 3)
        {
          nextNote = note - 12;//
        }
        else if (checkNote < 6)
        {
          nextNote = note + 12;//
        }
        else if (checkNote < 7)
        {
          nextNote = note - 10;//
        }
        else if (checkNote < 8)
        {
          nextNote = note + 9;//
        }
        else if (checkNote < 9)
        {
          nextNote = note - 7;//
        }
        else if (checkNote < 10)
        {
          nextNote = note + 8;//
        }
        else if (checkNote < 11)
        {
          nextNote = note - 5;//
        }
        else if (checkNote < 12)
        {
          nextNote = note + 7;//
        }
        else if (checkNote < 14)
        {
          nextNote = note - 4;//
        }
        else if (checkNote < 16)
        {
          nextNote = note + 5;//
        }
        else if (checkNote < 21)
        {
          nextNote = note - 3;//
        }
        else if (checkNote < 26)
        {
          nextNote = note + 2;//
        }
      }

      if (nextNote < 1) //Make sure no negative notes.
      {
        nextNote = nextNote + 12;
      }

      return nextNote;
    }

//Overrides
  @Override
  public void actionPerformed(ActionEvent evt) {
    System.out.println("Action Event Incorrect");
  }

  //WindowEvent Handler
  @Override
  public void windowClosing(WindowEvent evt) {
    System.exit(0);  //Terminate the program
  }

   //Needed for Compilation
   @Override public void windowOpened(WindowEvent evt) { }
   @Override public void windowClosed(WindowEvent evt) { }
   //Debugging Code
   @Override public void windowIconified(WindowEvent evt) { System.out.println("Window Iconified"); }
   @Override public void windowDeiconified(WindowEvent evt) { System.out.println("Window Deiconified"); }
   @Override public void windowActivated(WindowEvent evt) { System.out.println("Window Activated"); }
   @Override public void windowDeactivated(WindowEvent evt) { System.out.println("Window Deactivated"); }
}
