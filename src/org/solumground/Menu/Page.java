package org.solumground.Menu;

import org.solumground.*;
import org.solumground.Json.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.Calendar;

import static java.util.Calendar.*;
import static java.util.Calendar.SECOND;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUseProgram;

public class Page {
    public static Page[] Pages;
    public static int SelectedPage = 1;
    public static boolean Interupt = false;

    public static Font DefaultFont = new Font(Main.FontPath);
    public static Text GamePosText = new Text("",DefaultFont, .03f, new Vec3(1f,1f-0.12f,0));
    public static Text GameFpsText = new Text("",DefaultFont, .03f, new Vec3(1f,1f-0.06f,0));
    public static Text GameSelectedBlockText = new Text("", DefaultFont, 0.03f, new Vec3(1f,1f,0));

    public int ID;
    public String Name;
    public Text[] Texts;
    public Button[] Buttons;
    public boolean GameBackground;
    public boolean IsGame;

    public static void Wincallback(long window, int X, int Y){
        Main.win_X = X;
        Main.win_Y = Y;
        Main.aspectRatio = (float)X/(float)Y;
        glViewport(0,0, X, Y);
        Player.set_Projection();
    }
    public static void GameMouseMoveCallback(long window, double X, double Y){
        float distX = (float)(Main.mouse_past_X-X);
        float distY = (float)(Main.mouse_past_Y-Y);
        Main.mouse_past_X = X;
        Main.mouse_past_Y = Y;
        Player.Rotate_Y(Main.playerRotateSpeedMouse*Main.TimeElapsed*distX);
        Player.Rotate_X(Main.playerRotateSpeedMouse*Main.TimeElapsed*distY);
    }
    public static void GameMouseButtonCallback(long win, int button, int action, int mods){
        if(action == GLFW_PRESS){
            if(button == GLFW_MOUSE_BUTTON_RIGHT){
                Player.place_block();
            }
            if(button == GLFW_MOUSE_BUTTON_LEFT){
                Player.break_block();
            }
        }
    }
    public static void GameKeyCallback(long window, int key, int scancode, int action, int mods){
        if (key == GLFW_KEY_ESCAPE) {
            SelectedPage = 2;
            Interupt = true;
        }
        if(action == GLFW_PRESS){
            if(mods == 0){
                if(key == GLFW_KEY_F10){
                    int [] RawScreenData = new int[Main.win_X*Main.win_Y*3];
                    glReadBuffer(GL_BACK);
                    glReadPixels(0,0, Main.win_X,Main.win_Y, GL_RGB, GL_UNSIGNED_INT, RawScreenData);
                    int [] FlipedScreenData = new int[RawScreenData.length];
                    for(int Y=0;Y<Main.win_Y;Y++){
                        System.arraycopy(RawScreenData, (Main.win_Y-(Y+1))*Main.win_X*3, FlipedScreenData, Y*Main.win_X*3, Main.win_X*3);
                    }
                    BufferedImage img = new BufferedImage(Main.win_X, Main.win_Y, BufferedImage.TYPE_INT_RGB);
                    WritableRaster wr  = img.getRaster();
                    wr.setPixels(0,0,Main.win_X,Main.win_Y, FlipedScreenData);
                    Calendar Time = Calendar.getInstance();
                    String TimeString = Time.get(YEAR)+"."+Time.get(MONTH)+"."+Time.get(DAY_OF_MONTH)+":"+
                            Time.get(HOUR)+"."+Time.get(MINUTE)+"."+Time.get(SECOND);
                    File ScreenShotFile = new File(Main.jar_folder_path+"/screenshots/"+TimeString+".png");
                    ScreenShotFile.mkdirs();
                    try {
                        ImageIO.write(img, "png", ScreenShotFile);
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                    System.out.println("Saved ScreenShot as: "+TimeString+".png");
                    Console.Print("Saved ScreenShot as: "+TimeString+".png");
                }
            }
            if((mods & 0x0004) > 0) {
                if (key == GLFW_KEY_F) {
                    Main.FullScreen = !Main.FullScreen;
                    Main.win_X /= 2;
                    Main.win_Y /= 2;
                    Main.update_fullscreen();
                }
                if(key == GLFW_KEY_S){
                    Main.DrawSkyBox = !Main.DrawSkyBox;
                }
                if(key == GLFW_KEY_C){
                    Main.showCollisionBox = !Main.showCollisionBox;
                }
                if(key == GLFW_KEY_K) {
                    Player.is_flying = !Player.is_flying;
                }
                if(key == GLFW_KEY_R){
                    Console.Print("Respawning");
                    Player.reSpawn();
                }
                if(key == GLFW_KEY_EQUAL){
                    Main.RenderDistance++;
                    Player.ChunkReload = true;
                    Console.Print("Increased Render Distance To: "+Main.RenderDistance);
                }
                if(key == GLFW_KEY_MINUS){
                    Main.RenderDistance--;
                    Player.ChunkReload = true;
                    Console.Print("Decreased Render Distance To: "+Main.RenderDistance);
                }
            }
            if(key == GLFW_KEY_O){Player.move_hotbar(-1);}
            if(key == GLFW_KEY_P){Player.move_hotbar(1);}
            if((mods & 0x0002) > 0){
                if(key == GLFW_KEY_S){
                    System.out.print("Saving...");
                    Console.Print("Saving...");
                    for(int X=0;X<Main.ChunkCount;X++){
                        Main.ChunkArray[X].Save();
                    }
                    Player.Save();
                    System.out.println("Saved");
                    Console.Add("Saved");
                }
            }
        }

    }

    public static void MenuMouseMoveCallback(long win, double X, double Y){
        Main.mouse_past_X = X;
        Main.mouse_past_Y = Y;
    }
    public static void MenuMouseButtonCallback(long win, int button, int action, int mods){
        if(action == GLFW_PRESS){
            if(button == GLFW_MOUSE_BUTTON_LEFT) {
                for (Button button1 : Pages[SelectedPage].Buttons) {
                    if (button1.TestOver()) {
                        if(button1.OnPress == -1){
                            glfwSetWindowShouldClose(Main.win, true);
                            Interupt = true;
                            return;
                        }
                        SelectedPage = button1.OnPress;
                        Interupt = true;
                        break;
                    }
                }
            }
        }
    }
    public static void MenuKeyCallback(long win, int key, int scancode, int action, int mods){
        if(action == GLFW_PRESS) {
            if (key == GLFW_KEY_ESCAPE) {
                if(Pages[SelectedPage].Name.equals("Main")) {
                    glfwSetWindowShouldClose(Main.win, true);
                    Interupt = true;
                }
                if(Pages[SelectedPage].Name.equals("Pause")){
                    SelectedPage = 1;
                    Interupt = true;
                }
            }
        }
    }

    public static void Init(){
        File Pages_dir = new File(Main.jar_folder_path+"/assets/solumground/Pages");
        String [] filenames = Pages_dir.list();
        assert filenames != null;
        Pages = new Page[filenames.length];
        for (String filename : filenames) {
            Page page = new Page(Main.jar_folder_path + "/assets/solumground/Pages/" + filename);
            Pages[page.ID] = page;
        }
    }

    public static void Run(){
        glfwSetWindowSizeCallback(Main.win, Page::Wincallback);

        while(!glfwWindowShouldClose(Main.win)) {
            Interupt = false;
            Page page = Pages[SelectedPage];
            page.DO();
        }
        glfwDestroyWindow(Main.win);
        glfwTerminate();
    }

    public Page(String Path){
        try {
            JsonObject object = new JsonParser(Path).mainJsonObject;
            ID = object.Get("ID").GetInt();
            Name = object.Get("Name").GetString();
            GameBackground = object.Get("GameBackground").GetBoolean();
            IsGame = object.Get("IsGame").GetBoolean();
            JsonArray array = object.Get("Buttons").GetArray();
            Buttons = new Button[array.Size];
            for(int X=0;X<array.Size;X++){
                JsonObject object1 = array.Get(X).GetObject();
                Button button = new Button(
                        object1.Get("PosX").GetFloat(),
                        object1.Get("PosY").GetFloat(),
                        object1.Get("SizeX").GetFloat(),
                        object1.Get("SizeY").GetFloat(),
                        object1.Get("Text").GetString(),
                        object1.Get("OnPress").GetInt()
                );
                Buttons[X] = button;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void DrawGame(){
        glUseProgram(Main.MainShaderProgram);
        if(Main.DrawSkyBox) {
            SkyBox.draw();
        }
        Player.Draw();
        for(int X=0;X<Main.ChunkCount;X++){
            if(Main.ChunkArray[X] != null) {
                if(Main.ChunkArray[X].status == Chunk.Status.Complete) {
                    Main.ChunkArray[X].main_mesh.draw();
                }
            }
        }
        glUseProgram(Main.TextShaderProgram);
        GameFpsText.updateText("FPS:"+ Math.round(Main.fps));
        GameFpsText.render();
        GameSelectedBlockText.updateText("Selected Block:"+Block.Blocks[Player.hotbar_selected].Name);
        GameSelectedBlockText.render();
        GamePosText.updateText(String.format("POS: %s", Player.position));
        GamePosText.render();
        Console.Draw();
    }

    public void DO(){
        if(this.IsGame){
            Player.Load();
            Player.is_flying = false;

            glfwSetInputMode(Main.win, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            if (glfwRawMouseMotionSupported()) {
                glfwSetInputMode(Main.win, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);
            }
            glfwSetKeyCallback(Main.win, Page::GameKeyCallback);
            glfwSetCursorPosCallback(Main.win, Page::GameMouseMoveCallback);
            glfwSetMouseButtonCallback(Main.win, Page::GameMouseButtonCallback);
            glClearColor(0,0,0,1);

            Main.chunkLoader = new ChunkLoader();
            Main.chunkLoader.loadAroundPlayer(3);
            Main.chunkLoader.setDaemon(true);
            Main.chunkLoader.start();

            Main.meshBuilder = new MeshBuilder();
            Main.meshBuilder.setDaemon(true);
            Main.meshBuilder.start();

            double PastTime = glfwGetTime();
            while (!Interupt && !glfwWindowShouldClose(Main.win)){

                double CurentTime = glfwGetTime();
                Main.Time = (float)CurentTime;
                Main.TimeElapsed = (float)(CurentTime-PastTime);
                PastTime = CurentTime;
                Main.AvgTimeElapsed += (Main.TimeElapsed-Main.AvgTimeElapsed)/20;
                Main.fps = 1/Main.AvgTimeElapsed;

                Main.MakeGLCalls();
                glfwPollEvents();

                Main.playerMoveSpeedSprint = 1.0f;
                if (glfwGetKey(Main.win, GLFW_KEY_LEFT_SHIFT) == 1) {
                    if (Player.is_flying) {
                        Player.Move_Y(Main.playerMoveSpeed * Main.playerMoveSpeedSprint * Main.TimeElapsed * -1);
                    } else {
                        Main.playerMoveSpeedSprint = 1.5f;
                    }
                }
                if(glfwGetKey(Main.win, GLFW_KEY_SPACE) == 1){
                    if(Player.is_flying) {
                        Player.Move_Y(Main.playerMoveSpeed * Main.playerMoveSpeedSprint * Main.TimeElapsed);
                    }
                    else{
                        Player.jump();
                    }
                }
                if(glfwGetKey(Main.win, GLFW_KEY_W) == 1){Player.Move_Z(Main.playerMoveSpeed*Main.playerMoveSpeedSprint*Main.TimeElapsed);}
                if(glfwGetKey(Main.win, GLFW_KEY_A) == 1){Player.Move_X(Main.playerMoveSpeed*Main.playerMoveSpeedSprint*Main.TimeElapsed);}
                if(glfwGetKey(Main.win, GLFW_KEY_S) == 1){Player.Move_Z(Main.playerMoveSpeed*Main.playerMoveSpeedSprint*Main.TimeElapsed*-1);}
                if(glfwGetKey(Main.win, GLFW_KEY_D) == 1){Player.Move_X(Main.playerMoveSpeed*Main.playerMoveSpeedSprint*Main.TimeElapsed*-1);}
                if(glfwGetKey(Main.win, GLFW_KEY_LEFT) == 1){Player.Rotate_Y(Main.playerRotateSpeedKey*Main.TimeElapsed);}
                if(glfwGetKey(Main.win, GLFW_KEY_RIGHT) == 1){Player.Rotate_Y(Main.playerRotateSpeedKey*Main.TimeElapsed*-1);}
                if(glfwGetKey(Main.win, GLFW_KEY_UP) == 1){Player.Rotate_X(Main.playerRotateSpeedKey*Main.TimeElapsed);}
                if(glfwGetKey(Main.win, GLFW_KEY_DOWN) == 1){Player.Rotate_X(Main.playerRotateSpeedKey*Main.TimeElapsed*-1);}

                if(!Player.is_flying){
                    Player.applyGravity();
                }


                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                glUseProgram(Main.MainShaderProgram);
                Player.update();
                DrawGame();
                glfwSwapBuffers(Main.win);
            }

            Main.chunkLoader.close();
            Main.meshBuilder.close();
            System.out.print("Saving...");
            Console.Print("Saving...");
            while(Main.chunkLoader.isAlive()){
                try{
                    Thread.sleep(1);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
            for(int X=0;X<Main.ChunkCount;X++){
                Main.ChunkArray[X].Save();
            }
            Player.Save();
            System.out.println("Saved");
            Console.Add("Saved");
            return;


        }
        else{
            glfwSetInputMode(Main.win, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            glfwSetInputMode(Main.win, GLFW_RAW_MOUSE_MOTION, GLFW_FALSE);

            glfwSetCursorPosCallback(Main.win, Page::MenuMouseMoveCallback);
            glfwSetMouseButtonCallback(Main.win, Page::MenuMouseButtonCallback);
            glfwSetKeyCallback(Main.win, Page::MenuKeyCallback);

            if(!GameBackground) {
                glClearColor(.3f, .3f, .3f, 1);
            }


            while(!Interupt && !glfwWindowShouldClose(Main.win)) {
                double CurentTime = glfwGetTime();
                Main.Time = (float)CurentTime;

                glfwPollEvents();
                Main.MakeGLCalls();

                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                if(GameBackground){
                    DrawGame();
                }

                glUseProgram(Main.TextShaderProgram);
                glDisable(GL_DEPTH_TEST);
                for(Button button : Buttons){
                    button.Draw();
                }

                glfwSwapBuffers(Main.win);
            }
        }
    }
}
