from MqttListener import MqttListener


def start():
    # Use a breakpoint in the code line below to debug your script.
    # Press Ctrl+F8 to toggle the breakpoint.
    listener = MqttListener("listener")
    listener.connect(1883)
    listener.start_listening()


# Press the green button in the gutter to run the script.
if __name__ == '__main__':
    start()

# See PyCharm help at https://www.jetbrains.com/help/pycharm/
