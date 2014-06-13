#include <pebble.h>

static Window *window;
static TextLayer *top_layer;       // text that points to char compass.
static TextLayer *bottom_layer;    // text that points to numeric compass.
static TextLayer *direction_layer; // text that displays compass info.
static TextLayer *init_layer;      // text that tells you to open the Android app.
/*
const char *num_direction;
const char *char_direction;
*/
static AppSync sync; //APPSYNC
static uint8_t sync_buffer[64];

/* These are keys to the perpetually 
updating records from the android app. */
enum DirectionKey{
   DIRECTION_KEY = 0x0,
   NUM_DIRECTION_KEY = 0x1,
};

bool up_click=false;
bool down_click=false;

/* If App_Sync sends an error*/
static void sync_error_callback(DictionaryResult dict_error, AppMessageResult app_message_error, void *context) {
  APP_LOG(APP_LOG_LEVEL_DEBUG, "App Message Sync Error: %d", app_message_error);
}

// When Android app is sending new data
static void sync_tuple_changed_callback(const uint32_t key, const Tuple* new_tuple, const Tuple* old_tuple, void* context) {
 // get rid of "instruction" text.
 //updating the text layer with new directional data!
  switch (key) {
    case DIRECTION_KEY:
        if(down_click){
            text_layer_set_text(direction_layer, new_tuple->value->cstring);
        //    text_layer_set_text(init_layer, ""); 
        }
    break;
    case NUM_DIRECTION_KEY:
        if(up_click){
        APP_LOG(APP_LOG_LEVEL_INFO, new_tuple->value->cstring);
            text_layer_set_text(direction_layer, new_tuple->value->cstring);
        }
    break;
  }
 }
 
 
// These are the click handlers for each of the buttons.
static void select_click_handler(ClickRecognizerRef recognizer, void *context) {
  up_click = false;
  down_click = false;
  text_layer_set_text(init_layer, ""); 
}

static void up_click_handler(ClickRecognizerRef recognizer, void *context) {
  up_click = true;
  down_click = false;
  text_layer_set_text(init_layer, ""); 
  text_layer_set_background_color(top_layer, GColorBlack);
  text_layer_set_text_color(top_layer, GColorClear);
  text_layer_set_background_color(bottom_layer, GColorClear);
  text_layer_set_text_color(bottom_layer, GColorBlack);
}

static void down_click_handler(ClickRecognizerRef recognizer, void *context) {
  up_click = false;
  down_click = true;
  text_layer_set_text(init_layer, ""); 
  text_layer_set_background_color(bottom_layer, GColorBlack);
  text_layer_set_text_color(bottom_layer, GColorClear);
  text_layer_set_background_color(top_layer, GColorClear);
  text_layer_set_text_color(top_layer, GColorBlack);
}

static void click_config_provider(void *context) {
  window_single_click_subscribe(BUTTON_ID_SELECT, select_click_handler);
  window_single_click_subscribe(BUTTON_ID_UP, up_click_handler);
  window_single_click_subscribe(BUTTON_ID_DOWN, down_click_handler);
}
 
 //not really sure what this does, hoping it will become clear eventually.
 static void send_cmd(void) {
  Tuplet value = TupletInteger(1, 1);

  DictionaryIterator *iter;
  app_message_outbox_begin(&iter);

  if (iter == NULL) {
    return;
  }

  dict_write_tuplet(iter, &value);
  dict_write_end(iter);

  app_message_outbox_send();
}
 
 
  // This function will set up all the text layers and will initialize the App_Sync.
static void window_load(Window *window) {
  Layer *window_layer = window_get_root_layer(window);
  
  //setting up the layer that displays the cardinal direction.
  direction_layer = text_layer_create(GRect(0, 60, 144, 68));
  text_layer_set_text_color(direction_layer, GColorBlack);
  text_layer_set_background_color(direction_layer, GColorClear);
  text_layer_set_font(direction_layer, fonts_get_system_font(FONT_KEY_BITHAM_42_BOLD));
  text_layer_set_text_alignment(direction_layer, GTextAlignmentCenter);
  layer_add_child(window_layer, text_layer_get_layer(direction_layer));
  
  
  // Setting up top-most text layer. 
  top_layer = text_layer_create(GRect(50, 0, 100, 25));
  text_layer_set_text_color(top_layer, GColorBlack);
  text_layer_set_background_color(top_layer, GColorClear);
  text_layer_set_font(top_layer, fonts_get_system_font(FONT_KEY_GOTHIC_18_BOLD));
  text_layer_set_text_alignment(top_layer, GTextAlignmentCenter);
  layer_add_child(window_layer, text_layer_get_layer(top_layer));
  text_layer_set_text(top_layer, "Numeric");
  
  // Setting up bottom-most text layer. 
  bottom_layer = text_layer_create(GRect(50, 140, 100, 30));
  text_layer_set_text_color(bottom_layer, GColorBlack);
  text_layer_set_background_color(bottom_layer, GColorClear);
  text_layer_set_font(bottom_layer, fonts_get_system_font(FONT_KEY_GOTHIC_18_BOLD));
  text_layer_set_text_alignment(bottom_layer, GTextAlignmentCenter);
  layer_add_child(window_layer, text_layer_get_layer(bottom_layer));
  text_layer_set_text(bottom_layer, "Character");
  
  
  // Setting up "Open App" text layer. 
  init_layer = text_layer_create(GRect(0, 45, 144, 80));
  text_layer_set_text_color(init_layer, GColorBlack);
  text_layer_set_background_color(init_layer, GColorClear);
  text_layer_set_font(init_layer, fonts_get_system_font(FONT_KEY_GOTHIC_18_BOLD));
  text_layer_set_text_alignment(init_layer, GTextAlignmentCenter);
  text_layer_set_text(init_layer, "Please open the Android companion app, then choose a compass type.");  
  layer_add_child(window_layer, text_layer_get_layer(init_layer));
  
  
  Tuplet initial_values[]= {
    TupletCString(DIRECTION_KEY, "Loading...."),
    TupletCString(NUM_DIRECTION_KEY, "Loading...."),
   };
    
  
  // Initializing App_Sync!
  app_sync_init(&sync, sync_buffer, sizeof(sync_buffer), initial_values, ARRAY_LENGTH(initial_values), sync_tuple_changed_callback, sync_error_callback, NULL);

  send_cmd();
  
}

//when you close the app.
static void window_unload(Window *window) {
  //deinitialize app_sync.
  app_sync_deinit(&sync);
  //destroy textlayer.
  text_layer_destroy(direction_layer);
}

static void init(void) {
  window = window_create();
  //window_set_background_color(window, GColorBlack);
  window_set_fullscreen(window, true);
  window_set_click_config_provider(window, click_config_provider);
  window_set_window_handlers(window, (WindowHandlers) {
    .load = window_load,
    .unload = window_unload,
  });
    
  //declaring input/output buffer size in bytes
  const uint32_t inbound_size = 64;
  const uint32_t outbound_size = 64;
  app_message_open(inbound_size, outbound_size);
  
  const bool animated = true;
  window_stack_push(window, animated);
    
}

static void deinit(void) {
  window_destroy(window);
}

int main(void) {
  init();
  app_event_loop();
  deinit();
}
