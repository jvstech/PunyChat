//!
//! @title        Puny Chat Splash Frame
//! @file         SplashFrame.java
//! @author       Jonathan Smith (CIS106-HYB2)
//! @description  Displays a splash frame containing the logo and summary for
//!               my project titled Puny Chat.
//!

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import javax.imageio.ImageIO;
import javax.swing.*;

public class SplashFrame extends JDialog
{
  // This nested class inherits the JPanel component and adds the ability to
  // display an image.
  static class ImagePanel extends JPanel
  {
    // Field for the image to display
    private Image image_;

    // Constructor taking a single argument of the image to display
    public ImagePanel(Image img)
    {
      if (img != null)
      {
        image_ = img;
      }
    }

    // When the component is repainted, draw the image.
    @Override
    protected void paintComponent(Graphics g)
    {
      // Must call super.paintComponent() or the rendering of the component
      // will look strange with tearing and artifacts.
      super.paintComponent(g);
      g.drawImage(image_, 0, 0, this);
    }
  }

  // display component fields (I don't suffix these with an underscore as they
  // aren't used like typical member variables.)
  private static BufferedImage splashImage;
  private JButton closeButton;
  private JPanel imagePanel;
  private JScrollPane projectInfoScrollPane;
  private JTextArea projectInfoTextArea;

  // default constructor
  public SplashFrame()
  {
    initComponents();
  }

  // Frame closed event callback
  private void onClose()
  {
    // Perform clean-up of the form and exit the program.
    dispose();
  }

  // This is the primary function used to configure the components and display
  // aspects of the splash frame.
  private void initComponents()
  {
    // Construct the individual components
    imagePanel = new ImagePanel(GetSplashImage());
    closeButton = new JButton();
    projectInfoScrollPane = new JScrollPane();
    projectInfoTextArea = new JTextArea();

    // Configure the splash frame; make it so the application exits when the
    // close button ('X') is pressed.
    setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    setTitle("Puny Chat");
    setBackground(new Color(60, 63, 65));

    // Configure the splash image panel.
    imagePanel.setBackground(new Color(60, 63, 65));
    imagePanel.setPreferredSize(new Dimension(320, 160));
    GroupLayout imagePanelLayout = new GroupLayout(imagePanel);
    imagePanel.setLayout(imagePanelLayout);
    imagePanelLayout.setHorizontalGroup(
      imagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGap(0, 320, Short.MAX_VALUE)
    );
    imagePanelLayout.setVerticalGroup(
      imagePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGap(0, 160, Short.MAX_VALUE)
    );

    // Configure the close button and make it so the application ends when
    // clicked.
    closeButton.setText("Close");
    closeButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent evt)
      {
        onClose();
      }
    });

    // Configure the text box that will contain the project info summary text.
    projectInfoTextArea.setEditable(false);
    projectInfoTextArea.setBackground(new Color(97, 102, 106));
    projectInfoTextArea.setColumns(20);
    projectInfoTextArea.setFont(new Font("Consolas", 1, 12));
    projectInfoTextArea.setForeground(new Color(255, 255, 255));
    projectInfoTextArea.setLineWrap(true);
    projectInfoTextArea.setRows(5);
    projectInfoTextArea.setText(projectInfoText);
    projectInfoTextArea.setWrapStyleWord(true);
    projectInfoScrollPane.setViewportView(projectInfoTextArea);

    // Create the display layout for the components. This makes it so components
    // can be resized and moved automatically when the size of the splash frame
    // changes. This layout specifically avoids using GridBagLayout.
    GroupLayout layout = new GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
          .addContainerGap()
          .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
              .addComponent(imagePanel, GroupLayout.PREFERRED_SIZE,
                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
              .addGap(18, 18, 18)
              .addComponent(projectInfoScrollPane, GroupLayout.DEFAULT_SIZE, 398,
                Short.MAX_VALUE))
            .addGroup(GroupLayout.Alignment.TRAILING,
              layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(closeButton)))
          .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
          .addContainerGap()
          .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
            .addComponent(imagePanel, GroupLayout.PREFERRED_SIZE,
              GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addComponent(projectInfoScrollPane, GroupLayout.DEFAULT_SIZE, 299,
              Short.MAX_VALUE))
          .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
          .addComponent(closeButton)
          .addContainerGap())
    );

    // Resize the frame down to the minimum size needed to display the child
    // components.
    pack();
  }


  private static Image GetSplashImage()
  {
    // Only create the splash image if it hasn't been created already.
    if (splashImage == null)
    {
      // Decode the base-64 encoded splash image data
      byte[] imageBytes =
        Base64.getDecoder().decode(SplashFrame.imageBase64String);
      // Store the splash image data bytes in an input stream
      ByteArrayInputStream imageStream = new ByteArrayInputStream(imageBytes);
      try
      {
        // Load the image data
        splashImage = ImageIO.read(imageStream);
        imageStream.close();
      }
      catch (IOException imageReadEx)
      {
        // Do nothing; image will just be blank. :(
      }
    }

    return splashImage;
  }

  private static final String projectInfoText =
    "Puny Chat\n" +
      "Jonathan Smith\n" +
      "CIS106-HYB2 Fall 2019\n" +
      "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n\n\n" +
      "Puny Chat is an application used for secure text communication with " +
      "multiple individuals within the confines of an untrusted network. It " +
      "employs modern cryptography to encrypt messages sent between users. " +
      "All communications between any number of users using Puny Chat are both " +
      "unreadable and tamper-proof.";

  // This is the Puny Chat image logo encoded in base-64. I added it to the
  // bottom of the class so it would use the least valuable code real estate in
  // the file.
  private static final String imageBase64String =
    "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" +
      "aKlO6kpdtRwXGHRo66c6TrdUuPZF+9njuAQS2OvJE2S/PxXqY8/m6WkZ20x1Zvc1" +
      "E3xzdrNnGFQh94NjcEEgpviAAXhbyf72Ctcs/JYLPvteJ4RL6iiXlNjhNesYB+4c" +
      "qvPLfvcZnucXiilxP0nW77X0Y1dYNQluF22zHBXqC1gVTJahvV/bvN8y0q+jl1uZ" +
      "zQdcGZMRkNGvCRTiZRMgkouUWEtxCJD9sNhSiwC3aW0ihrJacV/UHxLl+H83HG/p" +
      "3hhluVbsdH0dty+Ju8X2pX7HS+7Uzg9Qp8J9XF2xcKgbfULd/GTp9s4z7CkyICoN" +
      "sxyVh1ku3Vf70gOtS6MJViHqfSu69aAN0X63xCIPrzzeHfcqsi8h2SwSDlnesfH+" +
      "yxo3HNr5SgRI57+uTp+ltYm931m63RqLaOrJbagoN7OgGjVPg/FWq8ZI69T0XavQ" +
      "+X3LrU7IzTp6huXSbwd+d5lpObXOKcLM2UXrrrMsh8pydNGa325b0P/ZAcFm0OsD" +
      "y6Fr5RqwwHIPXWx5202xRi0nWbR+l1M9iomQS+84ZOWPnnKmOmb8CuqrpYrAzF9y" +
      "uQCQQk5cGwmGG+DxzBNEWGnI27AiiSdOWRcbUL4awfy57lh7ifgSbpa2MdmJJ/Eb" +
      "1+pqwMTH+ni4wzTLyMAK5Z9dafLlsjpyn9qGNOM7GcI5mNX7O9ZQ3PsMz/OLiI5N" +
      "G8F6ekZaOdWsT+xNubontf2DUEtgoGPtYOG9pH2e7zHbCskSKbR4m7s/yOe3AvX2" +
      "CBxZ/4uIxsVqfQgQ6yvim+9sh9amdR2tigi4gyhYSPUZKen8b823slICKFyvH+lr" +
      "zkFfPsRMMjKo31zjmhNpIq8niUX2n+fcbRe7ExiXEC9tGC5L/E56+JQ9O2WtZXxv" +
      "rWWauNoyNJ9kb4j8Gvefb3ne/ciyaKwUU19MOHLKpsuamyPLbpasqxmyAJnUXCoP" +
      "b422t4rwT+l8pvVx/beDMh23UftvOHra1h86YRsPHrfNstQ+0bhbH4vTti4WpxzW" +
      "CmuArglWCx+FsEpYKaMFfCisEJbrnMtVvkRu+GxdG8zSb+o4TfWYfOKsTdR+fUWA" +
      "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" +
      "HvUAAAAASUVORK5CYII=";
}
