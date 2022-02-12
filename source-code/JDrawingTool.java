//  Import statements
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import java.util.*;

import java.io.*;

import javax.imageio.*;

/*
    JDrawingTool is the main class of the program. It sets up the window 
    environment and makes use of various functions from DrawingWindow.

*/

public class JDrawingTool extends JFrame{

    MainWindow main_window = new MainWindow();
    
    String current_path = System.getProperty("user.home");

    String image_name = "\\Untitled";
    String image_type = "png";
    
    BufferedImage icon_image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
    Graphics2D icon_graphics = icon_image.createGraphics();

    JMenuBar menu_bar = new JMenuBar();

    JMenu file_tab = new JMenu(" File ");
    JMenuItem file_new = new JMenuItem("New ");
    JMenuItem file_open = new JMenuItem("Open ");
    JMenuItem file_save = new JMenuItem("Save ");
    JMenuItem file_save_as = new JMenuItem("Save As  ");

    FileMenu file_menu = new FileMenu(new File(current_path));
   
    JMenu edit_tab = new JMenu(" Edit ");
    JMenuItem edit_undo = new JMenuItem("Undo ");
    JMenuItem edit_redo = new JMenuItem("Redo ");

    JToolBar action_bar = new JToolBar(" Tool Bar ");
    JToggleButton draw_button = new JToggleButton(" Draw  "){{ setAlignmentX(.5f);}};
    JToggleButton erase_button = new JToggleButton(" Erase  "){{ setAlignmentX(.5f);}};

    JPopupMenu draw_popup = new JPopupMenu();
    JMenu draw_size_menu = new JMenu(" Set Size ");
    ResizeMenuItem[] draw_size_array = new ResizeMenuItem[]{

        new ResizeMenuItem(new ImageIcon(createLineImage(2, 0)), 2),
        new ResizeMenuItem(new ImageIcon(createLineImage(4, 0)), 4),
        new ResizeMenuItem(new ImageIcon(createLineImage(6, 2)), 6),
        new ResizeMenuItem(new ImageIcon(createLineImage(8, 4)), 8),
        new ResizeMenuItem(new ImageIcon(createLineImage(10, 6)), 10)
        
    };

    JPopupMenu erase_popup = new JPopupMenu(); 
    JMenu erase_size_menu = new JMenu(" Set Size ");
    EraseResizeMenuItem[] erase_size_array = new EraseResizeMenuItem[]{

        new EraseResizeMenuItem(new ImageIcon(createEraseLineImage(10, 6)), 10),
        new EraseResizeMenuItem(new ImageIcon(createEraseLineImage(12, 6)), 12),
        new EraseResizeMenuItem(new ImageIcon(createEraseLineImage(14, 8)), 14),
        new EraseResizeMenuItem(new ImageIcon(createEraseLineImage(16, 10)), 16),
        new EraseResizeMenuItem(new ImageIcon(createEraseLineImage(18, 12)), 18)

    };

    public static void main(String[] args){     // static method which runs the program 
       
        SwingUtilities.invokeLater(new Runnable() 
        {
            public void run() 
            {
                new JDrawingTool();
            }
        });

    }

    public JDrawingTool(){
        
        /*
           Menu Bar Set-Up
        */

        file_tab.add(file_new);

        file_new.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){

                main_window.getDrawingWindow().clearRedoStack();
                main_window.getDrawingWindow().clearUndoStack();
                
                main_window.getDrawingWindow().getDrawingPad().clearCanvas();

                image_name = "\\Untitled";

                setTitle(" Untitled - JDrawingTool ");


            }
        }); 

        file_tab.add(file_open);

        file_open.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){

                Open();

            }
        }); 

        file_tab.add(file_save);

        file_save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){

                File temp_file = new File(current_path + image_name);

                if (temp_file.exists()){    

                    if(image_type == "png" ){

                        exportImage(main_window.getDrawingWindow().getDrawingPad().getImage(), 
                        temp_file.getAbsolutePath(), "png");

                        current_path = file_menu.getCurrentDirectory().getAbsolutePath();                        

                    }
                    else if (image_type == "jpg"){

                        exportImage(main_window.getDrawingWindow().getDrawingPad().getOpaqueImage(),
                        temp_file.getAbsolutePath(), "jpg");
                        
                        current_path = file_menu.getCurrentDirectory().getAbsolutePath();

                    }
                }
                else{   

                   SaveAs();

                }
            }
        });

        file_tab.add(file_save_as);

        file_save_as.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e){
                
                SaveAs();

            }
        });

        file_new.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        file_open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        file_save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        file_save_as.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));

        edit_tab.add(edit_undo);

        edit_undo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e){
               
                main_window.getDrawingWindow().undo();

            }
        });

        edit_tab.add(edit_redo);

        edit_redo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e){
               
                main_window.getDrawingWindow().redo();

            }
        });

        edit_undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        edit_redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));

        menu_bar.add(file_tab);
        menu_bar.add(edit_tab);

        setJMenuBar(menu_bar);

        /*
            Action Bar Set-Up
        */

        for (ResizeMenuItem menu_item: draw_size_array){

            draw_size_menu.add(menu_item);

        }

        draw_popup.add(draw_size_menu);    

        draw_size_array[0].setEnabled(false);
        
        draw_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                if(draw_button.isSelected()){
                    erase_button.setSelected(false);
                    main_window.getDrawingWindow().getDrawingPad().setMode("draw");
                }
                else{
                    main_window.getDrawingWindow().getDrawingPad().setMode("none");
                }
            }
        }); 

        draw_button.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {

                if (SwingUtilities.isRightMouseButton(e)){

                    draw_popup.show(e.getComponent(), e.getX(), e.getY());

                }
            }
         });


        for (EraseResizeMenuItem menu_item: erase_size_array){

            erase_size_menu.add(menu_item);

        }

        erase_popup.add(erase_size_menu);
      
        erase_size_array[0].setEnabled(false);

        erase_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                if(erase_button.isSelected()){
                    draw_button.setSelected(false);
                    main_window.getDrawingWindow().getDrawingPad().setMode("erase");
                    main_window.getDrawingWindow().getDrawingPad().drawEraser();
                }
                else{
                    main_window.getDrawingWindow().getDrawingPad().setMode("none");
                }
            }
        }); 

        erase_button.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {

                if (SwingUtilities.isRightMouseButton(e)){

                    erase_popup.show(e.getComponent(), e.getX(), e.getY());
                
                }
            }
        });

        draw_button.setBorder(new EmptyBorder(5, 5, 5, 5));
        erase_button.setBorder(new EmptyBorder(5, 5, 5, 5));

        draw_button.setToolTipText("<html>Keyboard Shortcut -> Alt+D<br>Right-Click -> Size Menu</html>");
        erase_button.setToolTipText("<html>Keyboard Shortcut -> Alt+E<br>Right-Click -> Size Menu</html>");

        PaintBar paint_bar = new PaintBar();
        
        action_bar.addSeparator(new Dimension(5, 0));  
        action_bar.add(draw_button);
        action_bar.add(erase_button);
       
        action_bar.addSeparator(new Dimension(5, 0));  
        action_bar.add(paint_bar);
        action_bar.addSeparator(new Dimension(5, 0)); 

        draw_button.setMnemonic(KeyEvent.VK_D);
        erase_button.setMnemonic(KeyEvent.VK_E);

        draw_button.setSelected(true);
    
        addWindowListener( new WindowAdapter() {
            public void windowOpened( WindowEvent e ){
                draw_button.requestFocus();
            }
        }); 

        /*
            Window Set-Up
        */

        setTitle(" Untitled - JDrawingTool ");

        icon_graphics.setColor(Color.black);
        icon_graphics.setFont(new Font("TimesRoman", Font.PLAIN, 36)); 
        icon_graphics.drawString("JDT", 0, 40);
        icon_graphics.dispose();

        setIconImage(icon_image);

        add(action_bar, BorderLayout.NORTH);
        add(main_window);
        
        setSize(new Dimension(550, 600));
        
        setLocationRelativeTo(null);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setVisible(true);
        
        action_bar.addMouseListener( 

            new MouseAdapter(){

                @Override
                public void mouseReleased(MouseEvent e){

                    paint_bar.setPosition(action_bar.getOrientation());

                    if (main_window.getDrawingWindow().getDrawingPad().getMode() == "erase"){

                        erase_button.requestFocus();
                        erase_button.requestFocusInWindow();    

                    }
                    else{

                        draw_button.requestFocus();
                        draw_button.requestFocusInWindow();    

                    }
                }
            }
        );
    }

    class MainWindow extends JPanel{   

        DrawingWindow drawing_window = new DrawingWindow();
    
        public MainWindow (){
    
            setBackground(Color.gray);
            setPreferredSize(new Dimension(500, 500));
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    
            add(drawing_window);
            
        }
    
        public DrawingWindow getDrawingWindow(){
    
            return drawing_window;
    
        }
    }
    
    class EraseResizeMenuItem extends JMenuItem{

        int size;

        public EraseResizeMenuItem(Icon icon, int size){

            super(icon);

            this.size = size;
                
            addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e){

                    main_window.getDrawingWindow().getDrawingPad().setEraserStrokeSize(size);
                    
                    for (EraseResizeMenuItem menu_item: erase_size_array){

                        menu_item.setEnabled(true);
            
                    }
                
                    setEnabled(false);

                    main_window.getDrawingWindow().getDrawingPad().setMode("erase");

                    erase_button.setSelected(true);
                    draw_button.setSelected(false);
                    erase_button.requestFocus();

                }
            });  
        }
    }

class ResizeMenuItem extends JMenuItem{

        int size;

        public ResizeMenuItem(Icon icon, int size){

            super(icon);

            this.size = size;
                
            addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e){

                    main_window.getDrawingWindow().getDrawingPad().setStrokeSize(size);
                    
                    for (ResizeMenuItem menu_item: draw_size_array){

                        menu_item.setEnabled(true);
            
                    }
                
                    setEnabled(false);

                    main_window.getDrawingWindow().getDrawingPad().setMode("draw");

                    draw_button.setSelected(true);
                    erase_button.setSelected(false);
                    draw_button.requestFocus();
                    

                }
            });  
        }
    }

    class FileMenu extends JFileChooser{

        public FileMenu(File file){

            super(file);

            setAcceptAllFileFilterUsed(false);

            addChoosableFileFilter(new FileNameExtensionFilter("PNG (*.png)", "png"));
            addChoosableFileFilter(new FileNameExtensionFilter("JPEG (*.jpg;*.jpeg;*.jpe;*.jfif)", "jpg" , "jpeg", "jpe", "jfif"));

            setSelectedFile(new File("Untiled"));
    
        }
    }

    class PaintBar extends JPanel{

        GridBagConstraints constraints;
        Color[] default_colors;
        Swatch[] swatch_array;

        public PaintBar(){
            
            default_colors = new Color[]{

                Color.black,                
                new Color(127, 127, 127),   // gray
                new Color(136,0, 21),       // maroon
                new Color(237, 28, 36),     // red
                new Color(255, 127, 39),    // orange
                new Color(255, 242, 0),     // yellow
                new Color(34, 177, 76),     // green
                new Color(0, 162, 232),     // blue
                new Color(63, 72, 204),     // dark blue
                new Color(163, 73, 164),    // purple
                Color.white,
                new Color(195, 195, 195),   // light gray
                new Color(185, 122, 87),    // brown
                new Color(255, 174, 201),   // light red
                new Color(255, 201, 14),    // light orange
                new Color(239, 228, 176),   // light yellow
                new Color(181, 230, 29),    // light green
                new Color(153, 217, 234),   // light blue
                new Color(112, 146, 190),   // steel blue
                new Color(200, 191, 231)    // light purple                                   

            };

            swatch_array = new Swatch[30];

            int count = 0;

            for( Color color: default_colors){

                swatch_array[count] = new Swatch(color);
                count += 1;

            }

            

            for (int i = count; i < swatch_array.length; i++){

                swatch_array[i] = new Swatch(Color.white);

            }

            setLayout(new GridBagLayout());
            constraints = new GridBagConstraints();

            constraints.insets = new Insets(1, 1, 1, 1);;

            setPosition(0);

            setFocusable(false);

        }

        public void setPosition(int orientation){

            removeAll();

            int color_count = 0;
    
            if (orientation == 0){      // horizontal

                for (int i = 0; i < 3; i++){

                    for (int j = 0; j < 10; j++){

                        constraints.gridx = j;
                        constraints.gridy = i;

                        add(swatch_array[color_count], constraints); 
                    
                        color_count += 1;

                    }
                }
                
                setThisSize(new Dimension(122, 38));    

            }
            else if (orientation == 1){     // vertical

                for (int i = 0; i < 3; i++){

                    for (int j = 0; j < 10; j++){

                        constraints.gridx = i;
                        constraints.gridy = j;

                        add(swatch_array[color_count], constraints);

                        color_count += 1;

                    }
                }
                
                setThisSize(new Dimension(38, 122));
            
            }

            revalidate();

        }

        class Swatch extends JPanel{

            Color swatch_color = null;
            BufferedImage swatch_image = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
            Graphics2D swatch_context = swatch_image.createGraphics();
            JPopupMenu edit_color_popup;
            ColorMenu edit_color_menu;
            
            public Swatch(Color swatch_color){

                this.swatch_color = swatch_color;

                setBackground(Color.black);

                setThisSize(new Dimension(10, 10));

                swatch_context.setColor(swatch_color);
                swatch_context.fillRect(0, 0, 19, 19);

                edit_color_popup = new JPopupMenu();
                edit_color_menu = new ColorMenu();

                edit_color_popup.add(edit_color_menu);

                setMouseListener();
                setToolTipText("<html>Right-Click -> Edit Color</html>");


            }

            @Override
            protected void paintComponent(Graphics g){
    
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.drawImage(swatch_image, 0, 0, 10, 10, null);        
                g2.dispose();
    
            }

            public void setMouseListener(){

                addMouseListener(
                
                    new MouseAdapter(){

                        @Override
                        public void mousePressed(MouseEvent e){

                            if(!SwingUtilities.isRightMouseButton(e)){
                                main_window.getDrawingWindow().getDrawingPad().setActiveColor(swatch_color);
                            }

                        }

                        public void mouseReleased(MouseEvent e){

                            if(SwingUtilities.isRightMouseButton(e)){

                                edit_color_popup.show(e.getComponent(), e.getX(), e.getY());
                            
                            }

                        }   
                    }
                );
            }

            class ColorMenu extends JPanel{

                JSlider red_slider; 
                JSlider green_slider; 
                JSlider blue_slider; 

                JPanel color_panel;
                JPanel slider_panel;

                GridBagConstraints constraints;

                public ColorMenu(){

                    setThisSize(new Dimension(315, 85));

                    JSlider red_slider = new JSlider(0, 255, swatch_color.getRed()); 
                    JSlider green_slider = new JSlider(0, 255, swatch_color.getGreen()); 
                    JSlider blue_slider = new JSlider(0, 255, swatch_color.getBlue()); 

                    red_slider.setPaintLabels(true);

                    color_panel = new JPanel();
                    slider_panel = new JPanel();

                    color_panel.setBackground(swatch_color);
            
                    color_panel.setPreferredSize(new Dimension(60,60));
                    color_panel.setMaximumSize(new Dimension(60,60));
                    color_panel.setMinimumSize(new Dimension(60,60));

                    slider_panel.setPreferredSize(new Dimension(220,75));
                    slider_panel.setMaximumSize(new Dimension(220,75));
                    slider_panel.setMinimumSize(new Dimension(220,75));

                    slider_panel.setLayout(new BoxLayout(slider_panel, BoxLayout.PAGE_AXIS));

                    slider_panel.add(new JPanel(){{  

                        setLayout(new FlowLayout());
                      
                        add(red_slider); 
                        add(new JLabel("R"));

                        setPreferredSize(new Dimension(220, 25));
                        setMaximumSize(new Dimension(220, 25));
                        setMinimumSize(new Dimension(220, 25));

                    }});
                      
                    slider_panel.add(new JPanel(){{  

                        setLayout(new FlowLayout());
                       
                        add(green_slider);  
                        add(new JLabel("G"));

                        setPreferredSize(new Dimension(220, 25));
                        setMaximumSize(new Dimension(220, 25));
                        setMinimumSize(new Dimension(220, 25));

                    }});
                    
                    slider_panel.add(new JPanel(){{  

                        setLayout(new FlowLayout());
                       
                        add(blue_slider);  
                        add(new JLabel("B"));

                        setPreferredSize(new Dimension(220, 25));
                        setMaximumSize(new Dimension(220, 25));
                        setMinimumSize(new Dimension(220, 25));

                    }});
        
                    add(color_panel);
                    add(slider_panel);           

                    MouseMotionAdapter a = new MouseMotionAdapter() {

                        public void mouseDragged(MouseEvent e){

                            swatch_color = new Color(red_slider.getValue(), green_slider.getValue(), blue_slider.getValue());
                            color_panel.setBackground(swatch_color);

                            swatch_context.setColor(swatch_color);
                            swatch_context.clearRect(0, 0, 19, 19);
                            swatch_context.fillRect(0, 0, 19, 19);

                            repaintSwatch();
                            revalidatePaintBar();
                            main_window.getDrawingWindow().getDrawingPad().setActiveColor(swatch_color);

                        }
                    };

                    red_slider.addMouseMotionListener(a);
                    green_slider.addMouseMotionListener(a);
                    blue_slider.addMouseMotionListener(a);       

                }

                public void setThisSize(Dimension dimension){

                    setPreferredSize(dimension);
                    setMaximumSize(dimension);
                    setMinimumSize(dimension);
    
                }
            }
            
            public void setThisSize(Dimension dimension){

                setPreferredSize(dimension);
                setMaximumSize(dimension);
                setMinimumSize(dimension);

            }

            public void repaintSwatch(){

                repaint();
                revalidate();

            }
        
        }

        public void setThisSize(Dimension dimension){

            setPreferredSize(dimension);
            setMaximumSize(dimension);
            setMinimumSize(dimension);

        }
    }

    public void Open(){

        file_menu.setDialogTitle(" Open ");

        int import_menu_result = file_menu.showOpenDialog(main_window);

        if(import_menu_result == 0){

            if(file_menu.getSelectedFile().exists()){

                BufferedImage input_image = importImage(file_menu.getSelectedFile());

                if (input_image != null){

                    main_window.getDrawingWindow().setThisWidth(
                        input_image.getWidth() +
                        main_window.getDrawingWindow().getWindowComp()
                    );
                    main_window.getDrawingWindow().setThisHeight(
                        input_image.getHeight() +
                        main_window.getDrawingWindow().getWindowComp()
                    );

                    main_window.getDrawingWindow().revalidate();

                    main_window.getDrawingWindow().getDrawingPad().setImage(input_image);

                    System.out.println(file_menu.getSelectedFile().getName());

                    image_name = "\\" + file_menu.getSelectedFile().getName();
                    setTitle(" " + file_menu.getSelectedFile().getName() + " - JDrawingTool");

                }

                current_path = file_menu.getCurrentDirectory().getAbsolutePath();

            }

            main_window.getDrawingWindow().clearRedoStack();
            main_window.getDrawingWindow().clearUndoStack();

        }
    }

    public void SaveAs(){
        
        file_menu.setDialogTitle(" Save As ");

        int export_menu_result = file_menu.showSaveDialog(main_window);

        if (export_menu_result == 0){
           
            if (file_menu.getFileFilter().accept(file_menu.getSelectedFile())){

                if(file_menu.getFileFilter().getDescription() == "PNG (*.png)"){

                    exportImage(main_window.getDrawingWindow().getDrawingPad().getImage(), 
                    file_menu.getSelectedFile().getAbsolutePath(), "png");

                    setTitle(" " + file_menu.getSelectedFile().getName() + " - JDrawingTool ");
                    
                    image_name = "\\" + file_menu.getSelectedFile().getName();
                    image_type = "png";

                }
                else if(file_menu.getFileFilter().getDescription() == "JPEG (*.jpg;*.jpeg;*.jpe;*.jfif)"){
                    
                    exportImage(main_window.getDrawingWindow().getDrawingPad().getOpaqueImage(), 
                    file_menu.getSelectedFile().getAbsolutePath(), "jpg");

                    setTitle(" " + file_menu.getSelectedFile().getName() + " - JDrawingTool ");

                    image_name = "\\" + file_menu.getSelectedFile().getName();
                    image_type = "jpg";

                } 
            }
            else{ 

                if(file_menu.getFileFilter().getDescription() == "PNG (*.png)"){

                    exportImage(main_window.getDrawingWindow().getDrawingPad().getImage(), 
                    file_menu.getSelectedFile().getAbsolutePath() + ".png", "png");

                    setTitle(" " + file_menu.getSelectedFile().getName() + ".png - JDrawingTool ");
                   
                    image_name = "\\" + file_menu.getSelectedFile().getName() + ".png";
                    image_type = "png";

                }
                else if(file_menu.getFileFilter().getDescription() == "JPEG (*.jpg;*.jpeg;*.jpe;*.jfif)"){

                    exportImage(main_window.getDrawingWindow().getDrawingPad().getOpaqueImage(), 
                    file_menu.getSelectedFile().getAbsolutePath() + ".jpg", "jpg");

                    setTitle(" " + file_menu.getSelectedFile().getName() + ".jpg - JDrawingTool ");
                    
                    image_name = "\\" + file_menu.getSelectedFile().getName() + ".jpg";
                    image_type = "jpg";

                } 

                current_path = file_menu.getCurrentDirectory().getAbsolutePath();

            }            
        }
    }

    public void revalidatePaintBar(){

        revalidate();

    }

    public void exportImage(BufferedImage image, String file_name, String file_type){

        try {
            File outputfile = new File(file_name);
            ImageIO.write(image, file_type, outputfile);
        } catch (IOException e) {}

    }

    
    public BufferedImage importImage(File input_file){

        BufferedImage input_image = null;

        try {
            input_image = ImageIO.read(input_file);
        } catch (IOException e) {}

        return input_image;

    }


    public BufferedImage createLineImage(int stroke, int pad){

        BufferedImage line_image = new BufferedImage(64, 16 + pad, BufferedImage.TYPE_INT_ARGB);
        Graphics2D line_context = line_image.createGraphics();

        line_context.setStroke(new BasicStroke(stroke, BasicStroke.JOIN_ROUND, BasicStroke.JOIN_ROUND));

        line_context.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        line_context.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        line_context.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        line_context.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        line_context.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        line_context.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        line_context.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        line_context.setColor(Color.black);
        line_context.drawLine(16, (16 + pad)/2, 56, (16 + pad)/2);

        return line_image;

    }

    public BufferedImage createEraseLineImage(int stroke, int pad){

        BufferedImage line_image = new BufferedImage(72, 16 + pad, BufferedImage.TYPE_INT_ARGB);
        Graphics2D line_context = line_image.createGraphics();

        line_context.setStroke(new BasicStroke(stroke));

        line_context.setColor(Color.black);
        line_context.drawLine(20, (16 + pad)/2, 60, (16 + pad)/2);

        return line_image;

    }
}


