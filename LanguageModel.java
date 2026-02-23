import java.util.HashMap;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
		In in = new In(fileName);
        String s = "";
        for (int i = 0; i < windowLength; i++)
        {
            s += in.readChar();
        }

        while (!in.isEmpty())
        {
            char c = in.readChar();
            if (CharDataMap.containsKey(s))
            {
                CharDataMap.get(s).update(c);
            }   
            else 
            {
                List lst = new List();
                lst.update(c);
                CharDataMap.put(s,lst);
            }
            s = s.substring(1) + c;
        }
        
        for(List val : CharDataMap.values())
        {
            calculateProbabilities(val);
        }
	}

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	void calculateProbabilities(List probs) {				
		Node current = new Node(probs.getFirst());				
		ListIterator lst = new ListIterator(current);
        int totalCount = 0;
        CharData cd;
        while (lst.hasNext())
        {
            cd = lst.next();
            totalCount += cd.count;
        }

        ListIterator lst2 = new ListIterator(current);
        double cp = 0;
        while (lst2.hasNext())
        {
            cd = lst2.next();
            double p = (double) cd.count / totalCount;
            cd.p = p;
            cp = p + cp;
            cd.cp = cp; 
        }
	}

    // Returns a random character from the given probabilities list.
	char getRandomChar(List probs) {
		double r = Math.random();

        ListIterator lst = probs.listIterator(0);
        while (lst.hasNext())
        {
            CharData cd = lst.next();
            if (cd.cp > r) 
            {
                return cd.chr;
            }
        }
		return ' ';
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
		if (initialText.length() < windowLength)
        {
            return initialText;
        }
        String generatedText = initialText;
        String window = initialText.substring(initialText.length() - windowLength);

        int count = 0;
        while (count < textLength)
        {
            List probs = CharDataMap.get(window);
            if (probs == null)
            {
                return generatedText;
            }
            char c = getRandomChar(probs);
            generatedText = generatedText + c;
            window = window.substring(1) + c;
            count++;
        }
        return generatedText;
	}

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
        int windowLength = Integer.parseInt(args[0]);
        String initialText = args[1];
        int generatedTextLength = Integer.parseInt(args[2]);
        Boolean randomGeneration = args[3].equals("random");
        String fileName = args[4];

        LanguageModel lm;
        if (randomGeneration)
            lm = new LanguageModel(windowLength);
        else
            lm = new LanguageModel(windowLength, 20);

        lm.train(fileName);

        System.out.println(lm.generate(initialText, generatedTextLength));
    }
}

