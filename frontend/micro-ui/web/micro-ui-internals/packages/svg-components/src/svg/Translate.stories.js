import React from "react";
import { Translate } from "./Translate";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Translate",
  component: Translate,
};

export const Default = () => <Translate />;
export const Fill = () => <Translate fill="blue" />;
export const Size = () => <Translate height="50" width="50" />;
export const CustomStyle = () => <Translate style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Translate className="custom-class" />;
export const Clickable = () => <Translate onClick={()=>console.log("clicked")} />;

const Template = (args) => <Translate {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};

