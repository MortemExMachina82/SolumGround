package org.solumground.GUI;

import org.solumground.*;
import org.solumground.Json.*;

import java.io.File;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glUniform1f;

public class Page {
    public static Page[] Pages;
    public static int SelectedPage = 1;
    public static boolean Interrupt = false;
    public static int VertexBufferObject = glGenBuffers();

    public static Text GamePosText = new Text("",Main.DefaultFont, .03f, new Vec3(1f,1f-0.12f,0));
    public static Text GameFpsText = new Text("",Main.DefaultFont, .03f, new Vec3(1f,1f-0.06f,0));
    public static Text GameSelectedBlockText = new Text("", Main.DefaultFont, 0.03f, new Vec3(1f,1f,0));

    public int ID;
    public String Name;
    public TextObject[] Texts = new TextObject[0];
    public Button[] Buttons = new Button[0];
    public Switch[] Switches = new Switch[0];
    public boolean GameBackground;
    public boolean IsGame;
    public Script OnExit;
    public String BackGroundTexture;
    public int TextureBufferObject;

    public static void SetPage(int ID){
        SelectedPage = ID;
        Interrupt = true;
    }
    public static Page GetPage(int ID){
        if(ID < 0){return Pages[0];}
        return Pages[ID];
    }

    public static void Wincallback(long window, int X, int Y){
        Main.win_X = X;
        Main.win_Y = Y;
        Main.aspectRatio = (float)X/(float)Y;
        glViewport(0,0, X, Y);
        Player.set_Projection(((float)Main.win_Y/Main.win_X), Main.FOV, Main.nearPlane, Main.farPlane);
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
            GetPage(SelectedPage).OnExit.Run();
        }
        if(action == GLFW_PRESS){
            if(mods == 0){
                switch(key) {
                    case GLFW_KEY_F10:
                        Main.TakeScreenShot();
                        break;
                    case GLFW_KEY_ENTER:
                        Player.place_block();
                        break;
                    case GLFW_KEY_BACKSPACE:
                        Player.break_block();
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
                    Main.SaveWorld();
                }
            }
            if(key == GLFW_KEY_Z){
                Main.playerRotateSpeedMouse *= 1.0/3;
                Main.playerRotateSpeedKey *= 1.0/3;
                Player.set_Projection(((float)Main.win_Y/Main.win_X), Main.ZoomFOV, Main.nearPlane, Main.farPlane);
            }
        }
        if(action == GLFW_RELEASE){
            if(key == GLFW_KEY_Z){
                Main.playerRotateSpeedMouse *= 3;
                Main.playerRotateSpeedKey *= 3;
                Player.set_Projection(((float)Main.win_Y/Main.win_X), Main.FOV, Main.nearPlane, Main.farPlane);
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
                for (Button button1 : GetPage(SelectedPage).Buttons) {
                    if (button1.TestOver()) {
                        button1.OnPress.Run();
                    }
                }
                for(Switch s : GetPage(SelectedPage).Switches){
                    if(s.TestOver()){
                        s.Toggle();
                    }
                }
            }
        }
    }
    public static void MenuKeyCallback(long win, int key, int scancode, int action, int mods){
        if(action == GLFW_PRESS) {
            if (key == GLFW_KEY_ESCAPE) {
                GetPage(SelectedPage).OnExit.Run();
            }
            if(key == GLFW_KEY_F){
                Main.FullScreen = !Main.FullScreen;
                Main.win_X /= 2;
                Main.win_Y /= 2;
                Main.update_fullscreen();
            }
        }
    }

    public static void Init(){
        File Pages_dir = new File(Main.RootDir +"/assets/solumground/Pages");
        String [] filenames = Pages_dir.list();
        assert filenames != null;
        Pages = new Page[filenames.length];
        for (String filename : filenames) {
            Page page = new Page(Main.RootDir + "/assets/solumground/Pages/" + filename);
            Pages[page.ID] = page;
        }
    }

    public static void Run(){
        glfwSetWindowSizeCallback(Main.win, Page::Wincallback);

        while(!glfwWindowShouldClose(Main.win)) {
            Interrupt = false;
            if(SelectedPage < 0){
                glfwSetWindowShouldClose(Main.win, true);
                continue;
            }
            Page page = GetPage(SelectedPage);
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
            this.BackGroundTexture = object.Get("BackGroundTexture").GetString();
            this.OnExit = new Script(object.Get("OnExit").GetString());
            JsonArray array = object.Get("Buttons").GetArray();
            if(array != null) {
                Buttons = new Button[array.Size];
                for (int X = 0; X < array.Size; X++) {
                    JsonObject object1 = array.Get(X).GetObject();
                    Button button = new Button(
                            object1.Get("PosX").GetFloat(),
                            object1.Get("PosY").GetFloat(),
                            object1.Get("SizeX").GetFloat(),
                            object1.Get("SizeY").GetFloat(),
                            object1.Get("BackGroundTexture").GetString(),
                            object1.Get("Text").GetString(),
                            object1.Get("OnPress").GetString()

                    );
                    Buttons[X] = button;
                }
            }
            array = object.Get("Switches").GetArray();
            if(array != null) {
                Switches = new Switch[array.Size];
                for (int X = 0; X < array.Size; X++) {
                    JsonObject object1 = array.Get(X).GetObject();
                    Switch s = new Switch(
                            object1.Get("PosX").GetFloat(),
                            object1.Get("PosY").GetFloat(),
                            object1.Get("SizeX").GetFloat(),
                            object1.Get("SizeY").GetFloat(),
                            object1.Get("TrueText").GetString(),
                            object1.Get("FalseText").GetString(),
                            object1.Get("BackGroundTexture").GetString(),
                            object1.Get("OnTrueScript").GetString(),
                            object1.Get("OnFalseScript").GetString()
                    );
                    Switches[X] = s;
                }
            }
            array = object.Get("Text").GetArray();
            if(array != null) {
                Texts = new TextObject[array.Size];
                for (int X = 0; X < array.Size; X++) {
                    JsonObject object1 = array.Get(X).GetObject();
                    TextObject text = new TextObject(
                            object1.Get("PosX").GetFloat(),
                            object1.Get("PosY").GetFloat(),
                            object1.Get("Size").GetFloat(),
                            object1.Get("Text").GetString()
                    );
                    Texts[X] = text;
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        this.TextureBufferObject = Main.LoadTexture(Main.RootDir +"/"+this.BackGroundTexture, 0x7F7F7FFF);
    }

    public static void DrawGame(){
        glUseProgram(Main.MainShaderProgram);
        if(Main.DrawSkyBox) {
            glUniform3f(Main.MainShader_U_MinLight, 0,0,0);
            SkyBox.Draw();
            glClear(GL_DEPTH_BUFFER_BIT);
        }
        glUniform3f(Main.MainShader_U_MinLight, .1f,.1f,.1f);

        Player.Draw();
        for(int X=0;X<Main.ChunkArray.size();X++){
            Chunk chunk = Main.ChunkArray.get(X);
            if(chunk != null) {
                if(chunk.status == Chunk.Status.Complete) {
                    chunk.main_mesh.draw();
                }
            }
        }
        glUseProgram(Main.TwoDShaderProgram);
        glDisable(GL_DEPTH_TEST);
        GameFpsText.updateText("FPS:"+ Math.round(Main.fps));
        GameFpsText.render();
        GameSelectedBlockText.updateText("Selected Block:"+Block.Blocks[Player.hotbar_selected].Name);
        GameSelectedBlockText.render();
        GamePosText.updateText(String.format("POS: %s", Player.position));
        GamePosText.render();
        Console.Draw();
        glEnable(GL_DEPTH_TEST);
    }
    public static void DrawBackGround(){
        glUseProgram(Main.TwoDShaderProgram);
        float [] VertexArray = new float[16];
        VertexArray[0] = -1;
        VertexArray[1] = -1;
        VertexArray[2] = 0;
        VertexArray[3] = 0;

        VertexArray[4] = -1;
        VertexArray[5] = 1;
        VertexArray[6] = 1;
        VertexArray[7] = 0;

        VertexArray[8] = 1;
        VertexArray[9] = 1;
        VertexArray[10] = 1;
        VertexArray[11] = 1;

        VertexArray[12] = 1;
        VertexArray[13] = -1;
        VertexArray[14] = 0;
        VertexArray[15] = 1;
        glBindBuffer(GL_ARRAY_BUFFER, VertexBufferObject);
        glBufferData(GL_ARRAY_BUFFER, VertexArray, GL_DYNAMIC_DRAW);

        glBindTexture(GL_TEXTURE_2D, GetPage(SelectedPage).TextureBufferObject);


        glVertexAttribPointer(Main.TwoDShader_Vertex, 2, GL_FLOAT, false, 16, 0);
        glVertexAttribPointer(Main.TwoDShader_TextureCords, 2, GL_FLOAT, false, 16, 8);

        glUniform2f(Main.TwoDShader_TextPos, 0, 0);
        glUniform1f(Main.TwoDShader_Alpha, 1);

        glDrawArrays(GL_QUADS, 0, 4);
    }

    public void DO(){
        if(this.IsGame){
            Player.Load();
            Player.is_flying = false;


            glfwSetInputMode(Main.win, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            //Comment Out When Debugging ^
            if (glfwRawMouseMotionSupported()) {
                glfwSetInputMode(Main.win, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);
            }
            glfwSetKeyCallback(Main.win, Page::GameKeyCallback);
            glfwSetCursorPosCallback(Main.win, Page::GameMouseMoveCallback);
            glfwSetMouseButtonCallback(Main.win, Page::GameMouseButtonCallback);

            Main.chunkLoader = new ChunkLoader();
            Main.chunkLoader.loadAroundPlayer(3);
            Main.chunkLoader.setDaemon(true);
            Main.chunkLoader.start();

            Main.meshBuilder = new MeshBuilder();
            Main.meshBuilder.setDaemon(true);
            Main.meshBuilder.start();

            SkyBox.SetSimple(Main.SkyBoxSimple);
            Main.skyBox = new SkyBox();
            Main.skyBox.setDaemon(true);
            Main.skyBox.setName("SkyBox");
            Main.skyBox.start();



            double PastTime = glfwGetTime();
            while (!Interrupt && !glfwWindowShouldClose(Main.win)){

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

            Main.chunkLoader.kill();
            Main.meshBuilder.kill();
            Main.skyBox.kill();
            while(Main.chunkLoader.isAlive()){
                try{
                    Main.MakeGLCalls();
                    Thread.sleep(1);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
            Main.SaveWorld();
            return;
        }
        else{
            glfwSetInputMode(Main.win, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            glfwSetInputMode(Main.win, GLFW_RAW_MOUSE_MOTION, GLFW_FALSE);

            glfwSetCursorPosCallback(Main.win, Page::MenuMouseMoveCallback);
            glfwSetMouseButtonCallback(Main.win, Page::MenuMouseButtonCallback);
            glfwSetKeyCallback(Main.win, Page::MenuKeyCallback);

            while(!Interrupt && !glfwWindowShouldClose(Main.win)) {
                double CurentTime = glfwGetTime();
                Main.Time = (float)CurentTime;

                Main.MakeGLCalls();

                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                if(GameBackground){
                    DrawGame();
                }
                else{
                    DrawBackGround();
                }

                glDisable(GL_DEPTH_TEST);
                glUseProgram(Main.TwoDShaderProgram);
                for(Button button : Buttons){
                    button.Draw();
                }
                for(Switch s : Switches){
                    s.Draw();
                }
                for(TextObject text : Texts){
                    text.Draw();
                }
                glEnable(GL_DEPTH_TEST);
                glfwSwapBuffers(Main.win);
                glfwPollEvents();
            }
        }
    }
}
