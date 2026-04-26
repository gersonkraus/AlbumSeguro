const crypto = require('crypto');

const gerarTokenAcesso = () => {
  return crypto.randomBytes(16).toString('hex').toUpperCase();
};

const gerarQRCode = async (dados) => {
  const QRCode = require('qrcode');
  return await QRCode.toDataURL(JSON.stringify(dados));
};

const calcularIdade = (dataNascimento) => {
  const hoje = new Date();
  let idade = hoje.getFullYear() - dataNascimento.getFullYear();
  const mes = hoje.getMonth() - dataNascimento.getMonth();

  if (mes < 0 || (mes === 0 && hoje.getDate() < dataNascimento.getDate())) {
    idade--;
  }

  return idade;
};

module.exports = { gerarTokenAcesso, gerarQRCode, calcularIdade };
