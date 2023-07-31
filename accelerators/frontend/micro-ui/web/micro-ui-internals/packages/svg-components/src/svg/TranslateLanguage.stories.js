import React from "react";
import { TranslateLanguage } from "./TranslateLanguage";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "TranslateLanguage",
  component: TranslateLanguage,
};

export const Default = () => <TranslateLanguage />;
export const Fill = () => <TranslateLanguage fill="blue" />;
export const Size = () => <TranslateLanguage height="50" width="50" />;
export const CustomStyle = () => <TranslateLanguage style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TranslateLanguage className="custom-class" />;
export const Clickable = () => <TranslateLanguage onClick={()=>console.log("clicked")} />;

const Template = (args) => <TranslateLanguage {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
