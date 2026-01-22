import React from "react";
import { Whatshot } from "./Whatshot";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Whatshot",
  component: Whatshot,
};

export const Default = () => <Whatshot />;
export const Fill = () => <Whatshot fill="blue" />;
export const Size = () => <Whatshot height="50" width="50" />;
export const CustomStyle = () => <Whatshot style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Whatshot className="custom-class" />;
export const Clickable = () => <Whatshot onClick={()=>console.log("clicked")} />;

const Template = (args) => <Whatshot {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
