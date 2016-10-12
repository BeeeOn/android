#coordinates are for screen 1080 x 1920


from deviceHandler import DeviceHandler



handler = DeviceHandler()
first_time = True

try:
	handler.press_button('base_guide_add_gate_next_button')
	handler.press_button('base_guide_add_gate_next_button')
	handler.press_button('base_guide_add_gate_next_button')
	handler.press_button('base_guide_add_gate_next_button')
	handler.press_button('base_guide_add_gate_next_button')
except:
	print('Next button was not found, I assume that the intro did not start, skipping this step...')
	first_time = False

handler.press_button('login_demo_button')
if first_time:
	handler.touch_the_screen(745,1017) # grant access to contacts

handler.touch_the_screen(962,1642)
handler.touch_the_screen(962,1372)

handler.press_button('base_guide_add_gate_next_button')
handler.press_button('base_guide_add_gate_next_button')
handler.press_button('gate_add_write_it_button')
handler.press_button('dialog_edit_text_input_layout')
handler.type_text('12345')

#while True:
#	handler.touch_the_screen_on_given_coordinates()

handler.touch_the_screen(836,751)
handler.touch_the_screen(836,751)
handler.touch_the_screen(836,751)

handler.compare_snapshots('ref-out/add_gateway_result.png')
