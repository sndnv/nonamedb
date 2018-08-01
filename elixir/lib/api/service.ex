defmodule Api.Service do
  use Plug.Router
  require Plug.Conn
  require Logger
  alias Storage.Memory.MemoryEngine, as: Engine

  plug :match
  plug :dispatch

  get "/:id" do
    case Engine.get(Store, id) do
      nil ->
        send_resp(conn, 404, "Not Found")

      result ->
        conn = put_resp_content_type(conn, "application/octet-stream")
        send_resp(conn, 200, result)
    end
  end

  post "/:id" do
    {:ok, data, conn} = read_body(conn)
    {response_code, response_message} = do_put("POST", id, data)
    send_resp(conn, response_code, response_message)
  end

  put "/:id" do
    {:ok, data, conn} = read_body(conn)
    {response_code, response_message} = do_put("PUT", id, data)
    send_resp(conn, response_code, response_message)
  end

  delete "/:id" do
    {response_code, response_message} = do_put("DELETE", id, [])
    send_resp(conn, response_code, response_message)
  end

  match _ do
    send_resp(conn, 404, "Not Found")
  end

  defp do_put(method, id, data) do
    case Engine.put(Store, id, data) do
      :done ->
        {200, ""}

      error ->
        Logger.error(
          "Error [#{error}] encountered while processing [#{method}}] request."
        )

        {500, "Request Failed"}
    end
  end
end
