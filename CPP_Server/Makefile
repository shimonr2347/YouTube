CXX = g++
CXXFLAGS = -std=c++17 -pthread -I/usr/include/nlohmann
TARGET = vid_rec_server
SRCS = main.cpp user_thread_manager.cpp request_handler.cpp video_manager.cpp
OBJS = $(SRCS:.cpp=.o)

all: $(TARGET)

$(TARGET): $(OBJS)
	$(CXX) $(CXXFLAGS) -o $@ $^

%.o: %.cpp
	$(CXX) $(CXXFLAGS) -c $< -o $@

clean:
	rm -f $(OBJS) $(TARGET)

run: $(TARGET)
	./$(TARGET)

.PHONY: all clean runF