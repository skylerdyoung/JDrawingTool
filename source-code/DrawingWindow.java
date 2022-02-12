//  Import statements
import javax.swing.*;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;

import java.util.*;

/*
    The DrawingWindow class exists purely as a container for DrawingPad.
    It allows the user to drag at the corners of the picture and make it
    larger or smaller.
*/

public class DrawingWindow extends JPanel{
    
    DrawingPad drawingPad = new DrawingPad();  

    int x, y;
    Dimension dimension;
    int window_comp;
    int difference_x, difference_y;

    ArrayList<Object> undo_stack = new ArrayList<Object>();
    ArrayList<Object> redo_stack = new ArrayList<Object>();

    ArrayList<BufferedImage> resize_undo_stack = new ArrayList<BufferedImage>();
    ArrayList<BufferedImage> resize_redo_stack = new ArrayList<BufferedImage>();

    boolean resize_check;
    
    public DrawingWindow  (){

        dimension = new Dimension(408, 408);

        window_comp = 8;

        setThisSize(dimension);

        setBackground(Color.gray);

        setAlignmentX(LEFT_ALIGNMENT);

        setLayout(new GridBagLayout());     // layout sets up the transparent handles against the DrawingPad
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = 0;
        add(drawingPad, constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 0;
        constraints.gridy = 1;
        add(new WindowHandle(Cursor.S_RESIZE_CURSOR), constraints);

        constraints.fill = GridBagConstraints.VERTICAL;
        constraints.gridx = 1;
        constraints.gridy = 0;
        add(new WindowHandle(Cursor.E_RESIZE_CURSOR), constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridx = 1;
        constraints.gridy = 1;
        add(new WindowHandle(Cursor.SE_RESIZE_CURSOR), constraints);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                x = e.getX();
                y = e.getY();
                difference_x = (int)getThisWidth()- x;  
                difference_y = (int)getThisHeight()- y;

                if (undo_stack.size() >= 50){
    
                    undo_stack.remove(0);
        
                }    

                undo_stack.add(new int[]{
                    (int)getThisWidth(),(int)getThisHeight()
                });

                drawingPad.addDrawingToResizeStack();
        
                resize_check = true;

            }

            @Override
            public void mouseReleased(MouseEvent e) {

                resize_check = false;

            }


        });

        addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {

                // if user is dragging at the bottom right corner of the window (resizes height and width while dragging)
                if ( (x >= drawingPad.getThisWidth()) && (y >= drawingPad.getThisHeight()) ){ 
                    
                    if ( ((e.getX() + difference_x) - window_comp) >= 1){
                        x = e.getX();
                        setThisWidth(x + difference_x);
                        drawingPad.setThisWidth((x - window_comp) + difference_x);
                    }

                    if ( ((e.getY() + difference_y) - window_comp) >= 1){
                        y = e.getY();
                        setThisHeight(y + difference_y);
                        drawingPad.setThisHeight((y - window_comp) + difference_y);
                    }

                    revalidate();

                }
                else if ( x >= drawingPad.getThisWidth()){   // if user is dragging at the right side of the window (width only)

                    if ( ((e.getX() + difference_x) - window_comp) >= 1){
                        x = e.getX();
                        setThisWidth(x + difference_x);
                        drawingPad.setThisWidth((x - window_comp) + difference_x);
                    }

                    revalidate();
                    
                }
                else if (y >= drawingPad.getThisHeight()){  // if user is dragging at the bottom of the window (height only)


                    if ( ((e.getY() + difference_y) - window_comp) >= 1){
                        y = e.getY();
                        setThisHeight(y + difference_y);
                        drawingPad.setThisHeight((y - window_comp) + difference_y);
                    }

                    revalidate();
                        
                }
            }
        });
    }

    public void undo(){
    
            if ( undo_stack.size() > 0 ){

                // calls if the undo action is a coordinate (denoting length and width of canvas) 
    
                if (undo_stack.get(undo_stack.size() - 1).getClass().getSimpleName().equals("int[]")){

                    int[] temp_array = (int[]) undo_stack.remove(undo_stack.size() - 1);

                    redo_stack.add(new int[]{ (int) getThisWidth(), (int) getThisHeight()});

                    setThisWidth(temp_array[0]);
                    setThisHeight(temp_array[1]);
    
                    drawingPad.setThisWidth(temp_array[0] - window_comp);
                    drawingPad.setThisHeight(temp_array[1] - window_comp );

                    drawingPad.undoClip();

                    repaint();
                    revalidate();
                    
                }
                else{  

                    drawingPad.undo();

                }

            }
    }

    public void redo(){
    
        if (redo_stack.size() > 0 ){
     
               // calls if the redo action is a coordinate (denoting length and width of canvas) 
               
            if (redo_stack.get(redo_stack.size() - 1).getClass().getSimpleName().equals("int[]")){

                int[] temp_array = (int[]) redo_stack.remove(redo_stack.size() - 1);

                undo_stack.add(new int[]{ (int) getThisWidth(), (int) getThisHeight()});

                drawingPad.redoClip();

                setThisWidth(temp_array[0]);
                setThisHeight(temp_array[1]);

                drawingPad.setThisWidth(temp_array[0] - window_comp);
                drawingPad.setThisHeight(temp_array[1] - window_comp );

                repaint();
                revalidate();
                
            }
            else{

                drawingPad.redo();

            }
        }
    }

    public int getWindowComp(){return window_comp;}

    public DrawingPad getDrawingPad(){return drawingPad;}

    public void setThisSize(Dimension dimension){

        this.dimension = dimension;

        setPreferredSize(dimension);
        setMaximumSize(dimension);
        setMinimumSize(dimension);

    }

    
    public void setThisWidth(int x){

        dimension.width = x;

        setPreferredSize(dimension);
        setMaximumSize(dimension);
        setMinimumSize(dimension);

    }

    public void setThisHeight(int y){

        dimension.height = y;

        setPreferredSize(dimension);
        setMaximumSize(dimension);
        setMinimumSize(dimension);
        
    }

    public double getThisHeight(){return dimension.getHeight();}

    public double getThisWidth(){return dimension.getWidth();}

    public void clearUndoStack(){

        undo_stack.clear();
        resize_undo_stack.clear();

    }

    public void clearRedoStack(){

        redo_stack.clear();
        resize_redo_stack.clear();

    }

    class WindowHandle extends JComponent {

        public WindowHandle(int cursorType){

            setMaximumSize(new Dimension(8, 8));
            setMinimumSize(new Dimension(8, 8));
            setPreferredSize(new Dimension(8, 8));

            setCursor(new Cursor(cursorType));
    
        }
    }

    class DrawingPad extends JPanel{    // all the drawing in the program takes place in this class

        Dimension canvas_dimension;
        BufferedImage image;  
        Graphics2D image_context;
      
        String mode = "draw";

        Color active = Color.black;
        Color secondary = Color.white;

        BufferedImage window_overlay;
        Graphics2D window_overlay_context;
    
        int stroke = 2;
        int eraser_stroke = 10;
    
        HashMap<String, Integer> modes;
    
        Cursor[] cursors;
        MouseAdapter[] mouse_listeners;
        MouseMotionAdapter[] mouse_motion_listeners;
        
        RenderingHints render_default;
    
        double[] prev, next;
        long ms;    // milliseconds

        Thread draw_thread;
        Thread erase_thread;
        boolean thread_check = true;
    
        public DrawingPad(){
    
            draw_thread = new Thread(new LineDrawer("draw"));
            erase_thread = new Thread(new LineDrawer("erase"));
    
            modes = new HashMap<String, Integer>(){{
                put("none", 0);
                put("draw", 1);
                put("erase", 2);
            }};
    
            cursors = new Cursor[]{
                new Cursor(Cursor.DEFAULT_CURSOR),  // none
                new Cursor(Cursor.CROSSHAIR_CURSOR),    // draw
                Toolkit.getDefaultToolkit().createCustomCursor( // erase
                    new BufferedImage( 32, 32, 
                    BufferedImage.TYPE_INT_ARGB), 
                    new Point(0, 0), "eraser"
                )
            };    
    
            mouse_listeners = new MouseAdapter[]{
                new MouseAdapter() {},  // none    
                new MouseAdapter() {    // draw
    
                    @Override
                    public void mousePressed(MouseEvent e) {

                        if( resize_check == false){
    
                            addDrawingToStack();
        
                            ms = new Date().getTime();
        
                            next = new double[]{(double) e.getX(), (double) e.getY()};
        
                            setRenderingHints();
        
                            image_context.setColor(active);
                            image_context.setStroke(new BasicStroke(stroke, BasicStroke.JOIN_ROUND, BasicStroke.JOIN_ROUND));
                            image_context.drawLine(e.getX(), e.getY(), e.getX(), e.getY());
        
                            repaint();
        
                            thread_check = true;
                            draw_thread = new Thread(new LineDrawer("draw"));
                            draw_thread.start();

                        }
                
                    }
    
                    @Override
                    public void mouseReleased(MouseEvent e){

                        thread_check = false;
    
                        repaint();
                    
                    }
                },
                new MouseAdapter() {    // erase
    
                    @Override
                    public void mousePressed(MouseEvent e) {

                        if (resize_check == false){
    
                            addDrawingToStack();
        
                            image_context.setRenderingHints(render_default);
        
                            drawEraser();
        
                            next = new double[]{(double) e.getX(), (double) e.getY()};
                            
                            image_context.setColor(secondary);
                            image_context.setStroke(new BasicStroke(eraser_stroke));
                            image_context.drawLine(e.getX(), e.getY(), e.getX(), e.getY());
                            
                            repaint();
        
                            thread_check = true;
                            erase_thread = new Thread(new LineDrawer("erase"));
                            erase_thread.start();

                        }
                
                    }
    
                    @Override
                    public void mouseReleased(MouseEvent e){
    
                        thread_check = false;
    
                        repaint();
    
                    }
    
                    @Override
                    public void mouseExited(MouseEvent e){
    
                        clearWindowOverlay();
                        
                    }
                }
            };
    
            mouse_motion_listeners = new MouseMotionAdapter[]{
                new MouseMotionAdapter() {},  // none
                new MouseMotionAdapter() {  // draw
    
                    
                    @Override
                    public void mouseDragged(MouseEvent e) {
    
                        long temp = new Date().getTime();
                        long difference = temp - ms;
    
                        if ((difference > 100) && (!(e.getX() == next[0] && e.getY() == next[1]))) {
                            prev = next;
                            next = new double[]{(double)e.getX() , (double)e.getY() };
    
                            image_context.drawLine((int)prev[0], (int)prev[1], e.getX(), e.getY());
    
                            repaint();
                        }   
                    }
                },
                new MouseMotionAdapter() {  // erase
    
                    
                    @Override
                    public void mouseDragged(MouseEvent e) {
                        
                        drawEraser();
    
                        long temp = new Date().getTime();
                        long difference = temp - ms;
    
                        if ((difference > 100) && (!(e.getX() == next[0] && e.getY() == next[1]))) {

                            prev = next;
                            next = new double[]{(double)e.getX() , (double)e.getY()};
    
                            image_context.drawLine((int)prev[0], (int)prev[1], e.getX(), e.getY());
    
                            repaint();
                        }
                        
                        
                    }
    
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        
                        drawEraser();
    
                    }
                }
            };
    
            setThisSize(new Dimension(400, 400));
    
            image = new BufferedImage( (int)getThisWidth() , (int)getThisHeight() , BufferedImage.TYPE_INT_ARGB);
            image_context = image.createGraphics();
    
            image_context.setColor(secondary);
            image_context.fillRect(0, 0, image.getWidth(), image.getHeight());
    
            window_overlay = new BufferedImage( (int)getThisWidth() , (int)getThisHeight() , BufferedImage.TYPE_INT_ARGB);
            window_overlay_context = window_overlay.createGraphics();
    
            setMode(mode);
    
            render_default = image_context.getRenderingHints();
    
        }
        
        class LineDrawer implements Runnable{
    
            /* 
                This class assists the MouseEvent in drawing smooth lines.
                It runs as a thread while the normal MouseEvent drawing is taking place.
            */

            String runnable_name;
            long difference;
            long temp;
    
            public LineDrawer(String runnable_name){
    
                this.runnable_name = runnable_name;
    
            }

            public void run()
            {
                while(thread_check){
                
                    double[] temp_coordinates = getMouseCoordinates(); 
    
                    if (temp_coordinates != null) {
                    
                        double temp_x = temp_coordinates[0];        
                        double temp_y = temp_coordinates[1];
    
                        if ((temp_x != next[0]) && (temp_y != next[1])) 
                        {
    
                            temp = new Date().getTime();
                            difference = temp - ms;
                            ms = temp;
                            prev = next;
    
                            next = new double[]{temp_x, temp_y};
        
                            image_context.drawLine((int)prev[0], (int)prev[1], (int)next[0], (int)next[1]);
        
                            repaint();
    
                        }   
                    }
                }
            }  
        }
    
        @Override
        protected void paintComponent(Graphics g){  
    
            super.paintComponent(g);
    
            Graphics2D g2 = (Graphics2D) g.create();
    
            g2.drawImage(image, 0,  0, image.getWidth(), image.getHeight(), null);
            g2.drawImage(window_overlay, 0, 0, window_overlay.getWidth(), window_overlay.getHeight(), null);
        
            g2.dispose();
    
        }
    
        public void addDrawingToStack(){    // backup for canvas drawing operaiton
    
            redo_stack.clear();
    
            if (undo_stack.size() >= 50){
    
                undo_stack.remove(0);
    
            }
    
            BufferedImage stack_image = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
            Graphics2D stack_image_context = stack_image.createGraphics();
            stack_image_context.drawImage(image, 0, 0, null);
    
            undo_stack.add(stack_image);
    
        }

            
        public void addDrawingToResizeStack(){   // backup for canvas resizing operation 
    
            redo_stack.clear();
    
            if (undo_stack.size() >= 50){
    
                undo_stack.remove(0);
    
            }
    
            BufferedImage stack_image = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
            Graphics2D stack_image_context = stack_image.createGraphics();
            stack_image_context.drawImage(image, 0, 0, null);
    
            resize_undo_stack.add(stack_image);
    
        }

        public void undoClip(){     // undo picture for resizing canvas
    
            if ( resize_undo_stack.size() > 0 ){
         
                BufferedImage resize_undo_image = (BufferedImage) resize_undo_stack.remove(resize_undo_stack.size() - 1);
    
                BufferedImage stack_image = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
                Graphics2D stack_image_context = stack_image.createGraphics();
                stack_image_context.drawImage(image, 0, 0, null);
    
                resize_redo_stack.add(stack_image);
    
                image_context.setColor(Color.white);
                image_context.fillRect(0, 0, image.getWidth(), image.getHeight());
    
                image_context.drawImage(resize_undo_image, 0, 0, null);
    
                repaint();
    
            }
        }

        public void redoClip(){     // redo picture for resizing canvas
         
    
            if ( resize_redo_stack.size() > 0 ){
         
                BufferedImage resize_redo_image = (BufferedImage) resize_redo_stack.remove(resize_redo_stack.size() - 1);
    
                BufferedImage stack_image = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
                Graphics2D stack_image_context = stack_image.createGraphics();
                stack_image_context.drawImage(image, 0, 0, null);
    
                resize_undo_stack.add(stack_image);
    
                image_context.setColor(Color.white);
                image_context.fillRect(0, 0, image.getWidth(), image.getHeight());
    
                image_context.drawImage(resize_redo_image, 0, 0, null);
    
                repaint();
    
            }
        }
    
        public void undo(){     
    
            if ( undo_stack.size() > 0 ){
         
                BufferedImage undo_image = (BufferedImage) undo_stack.remove(undo_stack.size() - 1);
    
                BufferedImage stack_image = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
                Graphics2D stack_image_context = stack_image.createGraphics();
                stack_image_context.drawImage(image, 0, 0, null);
    
                redo_stack.add(stack_image);
    
                image_context.setColor(Color.white);
                image_context.fillRect(0, 0, image.getWidth(), image.getHeight());
    
                image_context.drawImage(undo_image, 0, 0, null);
    
                repaint();
    
            }
        }
    
        public void redo(){
    
            if ( redo_stack.size() > 0 ){
    
                BufferedImage redo_image = (BufferedImage) redo_stack.remove(redo_stack.size() - 1);
    
                BufferedImage stack_image = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
                Graphics2D stack_image_context = stack_image.createGraphics();
                stack_image_context.drawImage(image, 0, 0, null);
    
                undo_stack.add(stack_image);
    
                image_context.setColor(Color.white);
                image_context.fillRect(0, 0, image.getWidth(), image.getHeight());
    
                image_context.drawImage(redo_image, 0, 0, null);
              
                repaint();
    
            }
        }
    
        public void setRenderingHints(){
    
            image_context.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            image_context.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            image_context.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            image_context.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            image_context.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
            image_context.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            image_context.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
            
        }
    
        public double[] getMouseCoordinates(){     
    
            if( getMousePosition() != null ){
    
                double x = MouseInfo.getPointerInfo().getLocation().getX() - getLocationOnScreen().getX();
                double y = MouseInfo.getPointerInfo().getLocation().getY() - getLocationOnScreen().getY();
            
                return new double[]{x, y};
    
            }
            return null;
    
        }
    
        public void drawEraser(){
    
            double[] temp_coordinates = getMouseCoordinates();
    
            if( temp_coordinates != null ){
    
                double x = temp_coordinates[0];
                double y = temp_coordinates[1];
    
                window_overlay = new BufferedImage( (int)getThisWidth() , (int)getThisHeight() , BufferedImage.TYPE_INT_ARGB);
                window_overlay_context = window_overlay.createGraphics();
    
                window_overlay_context.setColor(Color.black);
                window_overlay_context.setStroke(new BasicStroke(eraser_stroke));
                window_overlay_context.drawLine((int) x , (int) y , (int) x , (int) y );
                window_overlay_context.setColor(secondary);
                window_overlay_context.setStroke(new BasicStroke(eraser_stroke - 2));
                window_overlay_context.drawLine((int) x , (int) y , (int) x , (int) y );
    
                repaint();    
                
            }
        }
    
        public void setActiveColor(Color active){
    
            this.active = active;        
            image_context.setColor(active);
    
        }
    
        public void setMode(String mode){
            
            clearWindowOverlay();
    
            removeMouseListener(mouse_listeners[modes.get(this.mode)]);
            removeMouseMotionListener(mouse_motion_listeners[modes.get(this.mode)]);
    
            this.mode = mode;
            setCursor(cursors[modes.get(this.mode)]);
            
            addMouseListener(mouse_listeners[modes.get(this.mode)]);
            addMouseMotionListener(mouse_motion_listeners[modes.get(this.mode)]);
    
        }
    
    
        public void clearCanvas(){
    
            image = new BufferedImage( (int)getThisWidth() , (int)getThisHeight() , BufferedImage.TYPE_INT_ARGB);
            image_context = image.createGraphics();
    
            image_context.setColor(Color.white);
            image_context.fillRect(0, 0, image.getWidth(), image.getHeight());
    
            window_overlay = new BufferedImage( (int)getThisWidth() , (int)getThisHeight() , BufferedImage.TYPE_INT_ARGB);
            window_overlay_context = window_overlay.createGraphics();
    
            repaint();
    
        };
    
        public void setImage(BufferedImage input_image){    
            
            // sets up an imported image on the canvas 
            // resizes the canvas to fit the image
    
            image_context.setColor(Color.white);
            image_context.fillRect(0, 0, image.getWidth(), image.getHeight());
    
            setThisSize(new Dimension(input_image.getWidth(), input_image.getHeight()));
    
            image = new BufferedImage( (int)getThisWidth() , (int)getThisHeight() , BufferedImage.TYPE_INT_ARGB);
            image_context = image.createGraphics();
    
            window_overlay = new BufferedImage( (int)getThisWidth() , (int)getThisHeight() , BufferedImage.TYPE_INT_ARGB);
            window_overlay_context = window_overlay.createGraphics();
    
            image_context.drawImage(input_image, 0, 0, null);
    
            repaint();
    
        }
    
        public void clearWindowOverlay(){
                
            window_overlay = new BufferedImage( (int)getThisWidth() , (int)getThisHeight() , BufferedImage.TYPE_INT_ARGB);
            window_overlay_context = window_overlay.createGraphics();
    
            repaint();
        
        }
    
        
        public BufferedImage getOpaqueImage(){  // for JPEG images
            
            BufferedImage temp_image = new BufferedImage((int)getThisWidth() , (int)getThisHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D temp_image_graphics = temp_image.createGraphics();
            temp_image_graphics.drawImage(image, 0, 0, null);
            temp_image_graphics.dispose();
    
            return temp_image;
    
        }
    
        public BufferedImage getImage(){return image;}  // for PNG images 
    
        public String getMode(){return mode;}
    
        public double getThisHeight(){return canvas_dimension.getHeight();}
        
        public double getThisWidth(){return canvas_dimension.getWidth();}
    
        public void setStrokeSize(int stroke){this.stroke = stroke;}
    
        public void setEraserStrokeSize(int eraser_stroke){this.eraser_stroke = eraser_stroke;}
    
        public void setThisSize(Dimension dimension){
    
            canvas_dimension = dimension;
    
            setPreferredSize(dimension);
            setMaximumSize(dimension);
            setMinimumSize(dimension);
    
        }
        
        public void setThisWidth(int x){
    
            BufferedImage temp = image; 
    
            canvas_dimension.width = x;
    
            setPreferredSize(canvas_dimension);
            setMaximumSize(canvas_dimension);
            setMinimumSize(canvas_dimension);
    
            image = new BufferedImage( (int)getThisWidth() , (int)getThisHeight() , BufferedImage.TYPE_INT_ARGB);
            image_context = image.createGraphics();
    
            image_context.setColor(secondary);
            image_context.fillRect(0, 0, image.getWidth(), image.getHeight());
    
            image_context.drawImage(temp, 0, 0, temp.getWidth(), temp.getHeight(), null);
    
            window_overlay = new BufferedImage( (int)getThisWidth() , (int)getThisHeight() , BufferedImage.TYPE_INT_ARGB);
            window_overlay_context = window_overlay.createGraphics();
    
            window_overlay_context.drawImage(window_overlay, 0, 0, window_overlay.getWidth(), window_overlay.getHeight(), null);
    
        }
    
        public void setThisHeight(int y){
    
            BufferedImage temp = image; 
    
            canvas_dimension.height = y;
    
            setPreferredSize(canvas_dimension);
            setMaximumSize(canvas_dimension);
            setMinimumSize(canvas_dimension);
    
            image = new BufferedImage( (int)getThisWidth() , (int)getThisHeight() , BufferedImage.TYPE_INT_ARGB);
            image_context = image.createGraphics();
    
            image_context.setColor(secondary);
            image_context.fillRect(0, 0, image.getWidth(), image.getHeight());
    
            image_context.drawImage(temp, 0, 0, temp.getWidth(), temp.getHeight(), null);
    
            window_overlay = new BufferedImage( (int)getThisWidth() , (int)getThisHeight() , BufferedImage.TYPE_INT_ARGB);
            window_overlay_context = window_overlay.createGraphics();
    
            window_overlay_context.drawImage(window_overlay, 0, 0, window_overlay.getWidth(), window_overlay.getHeight(), null);
    
        }     
    }
}