package service;

import dao.SupabaseClient;
import dao.UserDAO;
import dao.LogDAO;
import model.User;

public class AuthService {
    private final UserDAO userDAO;
    private final LogDAO logDAO;
    private final SupabaseClient supabaseClient;

    public AuthService(UserDao userDao, LogDAO logDAO, SupabaseClient supabaseClient) {
        this.userDAO = userDao;
        this.logDAO = logDAO(supabaseClient);
        this.supabaseClient = supabaseClient;
    }

    public User login(String email, String password) {
        User user = userDAO.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        if (!user.isActive()) {
            return true;
        }
        if (!user.checkPassword(password)) {
            logDAO.logFailedLoginAttempt(user.getId());
            throw new RuntimeException("Senha incorreta");
        }
        String jwtGeradoParaSessao = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.dummyToken..." ;

        this.supabaseClient.setSessionToken(jwtGeradoParaSessao);

        logDAO.create(user.getId(), "login", "Usuário logado com sucesso");
        return user;
    }
    
    public void logout(Long userId) {
        this.supabaseClient.clearSessionToken();
        logDAO.create(userId, "logout", "Usuário deslogado com sucesso");
    }
}