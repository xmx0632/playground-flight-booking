package ai.spring.demo.ai.playground;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@Theme(value = "customer-support-agent")
public class Application implements AppShellConfigurator {

	private static final Logger logger = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		new SpringApplicationBuilder(Application.class).run(args);
	}

	// In the real world, ingesting documents would often happen separately, on a CI
	// server or similar.
	@Bean
	CommandLineRunner ingestTermOfServiceToVectorStore(
			EmbeddingModel embeddingModel, VectorStore vectorStore,
			@Value("classpath:rag/terms-of-service.txt") Resource termsOfServiceDocs) {

		return args -> {
			// Ingest the document into the vector store
			vectorStore.write(
					new TokenTextSplitter().transform(
							new TextReader(termsOfServiceDocs).read()));

			vectorStore.similaritySearch("Cancelling Bookings").forEach(doc -> {
				logger.info("Similar Document: {}", doc.getContent());
			});
		};
	}

	@Bean
	public VectorStore vectorStore(EmbeddingModel embeddingModel) {
		return new SimpleVectorStore(embeddingModel);
	}

	@Bean
	public ChatMemory chatMemory() {
		return new InMemoryChatMemory();
	}

	/**
	 * 没有远程 embedding api可以调用时 mock一个
	 * mock embeddingModel
	 * @return
	 */
//	@Bean
//	public EmbeddingModel embeddingModel() {
//		return new EmbeddingModel(){
//			@Override
//			public EmbeddingResponse call(EmbeddingRequest request) {
//				List<Embedding> list = new ArrayList<>();
//				list.add(new Embedding( new float[0],1));
//				return new EmbeddingResponse(list);
//			}
//
//			@Override
//			public float[] embed(String text) {
//				return new float[]{11,22,33};
//			}
//
//			@Override
//			public float[] embed(Document document) {
//				return new float[]{33,11,44};
//			}
//
//		};
//	}
}
