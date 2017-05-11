import datetime, os, signal, subprocess, sys, time, unittest

def run(command, stdin = None, timeout = 30):
    """
    Runs the specified command using specified standard input (if any) and
    returns the output on success. If the command doesn't return within the
    specified time (in seconds), "__TIMEOUT__" is returned.
    """

    start = datetime.datetime.now()
    process = subprocess.Popen(command.split(),
                               stdin = subprocess.PIPE, 
                               stdout = subprocess.PIPE,
                               stderr = subprocess.STDOUT)
    if not stdin is None:
        process.stdin.write(stdin)
        process.stdin.close()
    while process.poll() is None:
        time.sleep(0.1)
        now = datetime.datetime.now()
        if (now - start).seconds > timeout:
            os.kill(process.pid, signal.SIGKILL)
            os.waitpid(-1, os.WNOHANG)
            return "__TIMEOUT__"
    return process.stdout.read().strip()

class Problem1(unittest.TestCase):
    
    def test1(self):
        command = "java BrutePointST 0.661633 0.287141 0.65 0.68 0.28 0.29 5"
        sought = """st.empty()? false
st.size() = 10000
First 5 values:
  3380
  1585
  8903
  4168
  5971
  7265
st.contains((0.661633, 0.287141))? true
st.range([0.65, 0.68] x [0.28, 0.29]):
  (0.663908, 0.285337)
  (0.661633, 0.287141)
  (0.671793, 0.288608)
st.nearest((0.661633, 0.287141)) = (0.663908, 0.285337)
st.nearest((0.661633, 0.287141), 5):
  (0.663908, 0.285337)
  (0.658329, 0.290039)
  (0.671793, 0.288608)
  (0.65471, 0.276885)
  (0.668229, 0.276482)"""
        got = run(command, open("data/input10K.txt", "r").read())
        self.assertNotEquals(got, "__TIMEOUT__")
        self.assertEquals(got, sought)

class Problem2(unittest.TestCase):
    
    def test1(self):
        command = "java KdTreePointST 0.661633 0.287141 0.65 0.68 0.28 0.29 5"
        sought = """st.empty()? false
st.size() = 10000
First 5 values:
  0
  2
  1
  4
  3
  62
st.contains((0.661633, 0.287141))? true
st.range([0.65, 0.68] x [0.28, 0.29]):
  (0.671793, 0.288608)
  (0.663908, 0.285337)
  (0.661633, 0.287141)
st.nearest((0.661633, 0.287141)) = (0.663908, 0.285337)
st.nearest((0.661633, 0.287141), 5):
  (0.668229, 0.276482)
  (0.65471, 0.276885)
  (0.671793, 0.288608)
  (0.658329, 0.290039)
  (0.663908, 0.285337)"""
        got = run(command, open("data/input10K.txt", "r").read())
        self.assertNotEquals(got, "__TIMEOUT__")
        self.assertEquals(got, sought)

if __name__ == "__main__":
    unittest.main()
