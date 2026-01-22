import React from "react";
import { Article } from "./Article";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Article",
  component: Article,
};

export const Default = () => <Article />;
export const Fill = () => <Article fill="blue" />;
export const Size = () => <Article height="50" width="50" />;
export const CustomStyle = () => <Article style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Article className="custom-class" />;
export const Clickable = () => <Article onClick={()=>console.log("clicked")} />;

const Template = (args) => <Article {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
